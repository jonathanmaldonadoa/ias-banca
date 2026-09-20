package com.banco.ias.business.service;

import java.util.Objects;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.banco.ias.business.mapper.TransferenciaMapper;
import com.banco.ias.business.rules.TransferenciaBusinessRules;
import com.banco.ias.core.exception.BusinessRuleException;
import com.banco.ias.core.exception.IdempotencyConflictException;
import com.banco.ias.domain.dto.TransferenciaRequest;
import com.banco.ias.domain.dto.TransferenciaResponseDTO;
import com.banco.ias.domain.model.EstadoTransferencia;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.CuentaRepository;
import com.banco.ias.domain.repository.TransferenciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class TransferenciaServiceImpl implements TransferenciaService {
	private final TransferenciaRepository transferenciaRepository;
	private final TransferenciaBusinessRules rules;
	private final TransferenciaMapper mapper;
	private final CuentaRepository cuentaRepository;
	private final TransactionalOperator transactionalOperator;
	private final TransferenciaQueueService queueService;
	private final OutboxService outboxService;
	private final Sinks.Many<TransferenciaResponseDTO> stateEvents = Sinks.many().replay().limit(1000);

	public TransferenciaServiceImpl(TransferenciaRepository transferenciaRepository, TransferenciaBusinessRules rules,
			TransferenciaMapper mapper, CuentaRepository cuentaRepository, TransactionalOperator transactionalOperator,
			TransferenciaQueueService queueService, OutboxService outboxService) {
		this.transferenciaRepository = transferenciaRepository;
		this.rules = rules;
		this.mapper = mapper;
		this.cuentaRepository = cuentaRepository;
		this.transactionalOperator = transactionalOperator;
		this.queueService = queueService;
		this.outboxService = outboxService;
	}

	@PostConstruct
	void iniciarProcesador() {
		queueService.mensajes()
				.flatMap(message -> resolverPendiente(message.requestReference()).doOnSuccess(ignored -> message.ack()))
				.doOnError(
						error -> System.err.println("Error en el consumidor de transferencias: " + error.getMessage()))
				.retryWhen(Retry.backoff(Long.MAX_VALUE, java.time.Duration.ofSeconds(5))).subscribe();
	}

	@Override
	public Mono<TransferenciaResponseDTO> registrar(TransferenciaRequest request) {
		Transferencia transferencia = mapper.toDomain(request);
		if (transferencia == null) {
			return Mono.<TransferenciaResponseDTO>empty();
		}

		String reference = transferencia.requestReference() == null || transferencia.requestReference().isBlank()
				? UUID.randomUUID().toString()
				: transferencia.requestReference();
		Transferencia nueva = crearNuevaTransferencia(transferencia, reference);

		return rules.validarRF01(transferencia)
				.then(transactionalOperator.transactional(transferenciaRepository.insertIfAbsent(nueva)
						.flatMap(insertada -> outboxService.enqueue(reference).then(persistirTransferencia(insertada)))
						.switchIfEmpty(Mono.<TransferenciaResponseDTO>defer(
								() -> transferenciaRepository.findByRequestReference(reference).flatMap(existente -> {
									if (!mismaSolicitud(transferencia, existente)) {
										return Mono.<TransferenciaResponseDTO>error(new IdempotencyConflictException(
												"La requestReference ya fue utilizada con otros datos: " + reference));
									}
									return Mono.just(Objects.requireNonNull(mapper.toResponse(existente)));
								})))));
	}

	private Mono<TransferenciaResponseDTO> resolverPendiente(String reference) {
		return transferenciaRepository.findByRequestReference(reference)
				.filter(transferencia -> transferencia.estado() == EstadoTransferencia.PENDIENTE)
				.flatMap(transferencia -> rules.validarRF02(transferencia)
						.then(transferenciaRepository.findAcumuladoDiarioPorCuenta(transferencia.sourceAccountId()))
						.flatMap(acumulado -> rules.validarRF04(transferencia.sourceAccountId(), transferencia.amount(),
								acumulado))
						.then(rules.validarRF08(transferencia))
						.then(transactionalOperator.transactional(persistirTransferencia(aprobar(transferencia))))
						.onErrorResume(BusinessRuleException.class,
								ex -> transactionalOperator.transactional(persistirTransferencia(rechazar(transferencia,
										ex.getMessage(), EstadoTransferencia.RECHAZADA, "RECHAZADA"))))
						.onErrorResume(ex -> transactionalOperator.transactional(persistirTransferencia(
								rechazar(transferencia, "Error técnico durante el procesamiento: " + ex.getMessage(),
										EstadoTransferencia.RECHAZADA, "RECHAZADA"))))
						.doOnNext(response -> stateEvents.tryEmitNext(response)));
	}

	private boolean mismaSolicitud(Transferencia solicitada, Transferencia existente) {
		return solicitada.sourceAccountId().equals(existente.sourceAccountId())
				&& solicitada.destinationAccountId().equals(existente.destinationAccountId())
				&& solicitada.amount().compareTo(existente.amount()) == 0
				&& solicitada.currency().equalsIgnoreCase(existente.currency());
	}

	private Mono<TransferenciaResponseDTO> persistirTransferencia(Transferencia transferencia) {
		return transferenciaRepository.save(transferencia).flatMap(guardada -> {
			if (guardada.estado() != EstadoTransferencia.APROBADA) {
				return Mono.just(guardada);
			}
			return Mono
					.zip(cuentaRepository.findByNumero(guardada.sourceAccountId()),
							cuentaRepository.findByNumero(guardada.destinationAccountId()))
					.flatMap(cuentas -> Mono
							.zip(cuentaRepository.save(cuentas.getT1().debitar(guardada.amount())),
									cuentaRepository.save(cuentas.getT2().acreditar(guardada.amount())))
							.thenReturn(guardada));
		}).map(transferenciaProcesada -> Objects.requireNonNull(mapper.toResponse(transferenciaProcesada)));
	}

	private Transferencia crearNuevaTransferencia(Transferencia transferencia, String reference) {
		return new Transferencia(transferencia.id() == null ? UUID.randomUUID() : transferencia.id(), reference,
				transferencia.sourceAccountId(), transferencia.destinationAccountId(), transferencia.amount(),
				transferencia.currency(), EstadoTransferencia.PENDIENTE, "PENDIENTE",
				"Transferencia registrada para validación", transferencia.createdAt(), transferencia.updatedAt());
	}

	private Transferencia aprobar(Transferencia transferencia) {
		return transferencia.conResultado(EstadoTransferencia.APROBADA, "OK",
				"Transferencia aprobada y registrada correctamente");
	}

	private Transferencia rechazar(Transferencia transferencia, String motivo, EstadoTransferencia estado,
			String resultado) {
		return transferencia.conResultado(estado, resultado, motivo);
	}

	@Override
	public Mono<TransferenciaResponseDTO> consultarPorRequestReference(String requestReference) {
		return transferenciaRepository.findByRequestReference(requestReference)
				.map(transferencia -> Objects.requireNonNull(mapper.toResponse(transferencia)));
	}

	@Override
	public Flux<TransferenciaResponseDTO> listar() {
		return transferenciaRepository.findAll()
				.map(transferencia -> Objects.requireNonNull(mapper.toResponse(transferencia)));
	}

	@Override
	public Flux<TransferenciaResponseDTO> consultarPorCuenta(String accountNumber) {
		return transferenciaRepository.findByCuenta(accountNumber)
				.map(transferencia -> Objects.requireNonNull(mapper.toResponse(transferencia)));
	}

	@Override
	public Flux<TransferenciaResponseDTO> eventos(String requestReference) {
		return transferenciaRepository.findByRequestReference(requestReference).flatMapMany(actual -> {
			TransferenciaResponseDTO actualDto = Objects.requireNonNull(mapper.toResponse(actual));
			if (actual.estado() == EstadoTransferencia.APROBADA || actual.estado() == EstadoTransferencia.RECHAZADA) {
				return Flux.just(actualDto);
			}
			return stateEvents.asFlux().filter(response -> requestReference.equals(response.requestReference()));
		});
	}

	@Override
	public Transferencia toDomain(TransferenciaRequest request) {
		return mapper.toDomain(request);
	}
}
