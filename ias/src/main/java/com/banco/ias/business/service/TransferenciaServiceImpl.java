package com.banco.ias.business.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banco.ias.business.mapper.TransferenciaMapper;
import com.banco.ias.business.rules.TransferenciaBusinessRules;
import com.banco.ias.core.exception.BusinessRuleException;
import com.banco.ias.domain.dto.TransferenciaRequest;
import com.banco.ias.domain.dto.TransferenciaResponseDTO;
import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.model.EstadoTransferencia;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.CuentaRepository;
import com.banco.ias.domain.repository.TransferenciaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransferenciaServiceImpl implements TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaBusinessRules transferenciaBusinessRules;
    private final TransferenciaMapper transferenciaMapper;
    private final CuentaRepository cuentaRepository;

    public TransferenciaServiceImpl(TransferenciaRepository transferenciaRepository,
                                   TransferenciaBusinessRules transferenciaBusinessRules,
                                   TransferenciaMapper transferenciaMapper,
                                   CuentaRepository cuentaRepository) {
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaBusinessRules = transferenciaBusinessRules;
        this.transferenciaMapper = transferenciaMapper;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public Mono<TransferenciaResponseDTO> registrar(TransferenciaRequest request) {
        Transferencia transferencia = toDomain(request);

        if (transferencia == null) {
            return Mono.empty();
        }

        String requestReference = transferencia.requestReference() == null || transferencia.requestReference().isBlank()
                ? UUID.randomUUID().toString()
                : transferencia.requestReference();
        String sourceAccountKey = transferencia.sourceAccountId();
        Transferencia nueva = crearNuevaTransferencia(transferencia, requestReference);

        return transferenciaRepository.findByRequestReference(requestReference)
                .flatMap(transferenciaExistente -> {
                    transferenciaBusinessRules.validarRF05(requestReference, transferenciaExistente);
                    return Mono.<TransferenciaResponseDTO>empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    try {
                        transferenciaBusinessRules.validarRF01(transferencia);
                        transferenciaBusinessRules.validarRF02(transferencia);
                        transferenciaBusinessRules.validarRF03(transferencia);

                        return transferenciaRepository.findAcumuladoDiarioPorCuenta(sourceAccountKey)
                                .flatMap(acumuladoActual -> {
                                    transferenciaBusinessRules.validarRF04(sourceAccountKey, transferencia.amount(), acumuladoActual);
                                    transferenciaBusinessRules.validarRF08(transferencia);

                                    Transferencia aprobada = aprobarTransferencia(nueva);
                                    return persistirTransferencia(aprobada);
                                });
                    } catch (BusinessRuleException ex) {
                        return persistirTransferencia(rechazarTransferencia(nueva, ex.getMessage()))
                                .then(Mono.<TransferenciaResponseDTO>error(ex));
                    }
                }))
                .onErrorResume(BusinessRuleException.class, ex -> {
                    EstadoTransferencia estado = ex.getMessage() != null && ex.getMessage().startsWith("RF05:")
                            ? EstadoTransferencia.DUPLICADA
                            : EstadoTransferencia.RECHAZADA;
                    String resultado = estado == EstadoTransferencia.DUPLICADA ? "DUPLICADA" : "RECHAZADA";
                    return persistirTransferencia(rechazarTransferencia(nueva, ex.getMessage(), estado, resultado))
                            .then(Mono.<TransferenciaResponseDTO>error(ex));
                });
    }

    private Transferencia crearNuevaTransferencia(Transferencia transferencia, String requestReference) {
        return new Transferencia(
                transferencia.id() == null ? UUID.randomUUID() : transferencia.id(),
                requestReference,
                transferencia.sourceAccountId(),
                transferencia.destinationAccountId(),
                transferencia.amount(),
                transferencia.currency(),
                EstadoTransferencia.PENDIENTE,
                "PENDIENTE",
                "Transferencia registrada para validación",
                transferencia.createdAt(),
                transferencia.updatedAt()
        );
    }

    private Transferencia aprobarTransferencia(Transferencia transferencia) {
        return transferencia.conResultado(
                EstadoTransferencia.APROBADA,
                "OK",
                "Transferencia aprobada y registrada correctamente"
        );
    }

    private Transferencia rechazarTransferencia(Transferencia transferencia, String motivo) {
        return rechazarTransferencia(transferencia, motivo, EstadoTransferencia.RECHAZADA, "RECHAZADA");
    }

    private Transferencia rechazarTransferencia(Transferencia transferencia, String motivo,
                                               EstadoTransferencia estado, String resultado) {
        return transferencia.conResultado(estado, resultado, motivo);
    }

    private Mono<TransferenciaResponseDTO> persistirTransferencia(Transferencia transferencia) {
        return transferenciaRepository.save(transferencia)
                .doOnNext(transferenciaGuardada -> {
                    if (transferenciaGuardada.estado() == EstadoTransferencia.APROBADA) {
                        Cuenta origen = cuentaRepository.findByNumero(transferenciaGuardada.sourceAccountId());
                        Cuenta destino = cuentaRepository.findByNumero(transferenciaGuardada.destinationAccountId());

                        if (origen != null) {
                            cuentaRepository.save(origen.debitar(transferenciaGuardada.amount()));
                        }

                        if (destino != null) {
                            cuentaRepository.save(destino.acreditar(transferenciaGuardada.amount()));
                        }
                    }
                })
                .map(transferenciaMapper::toResponse);
    }

    @Override
    public Mono<TransferenciaResponseDTO> consultarPorRequestReference(String requestReference) {
        return transferenciaRepository.findByRequestReference(requestReference)
                .map(transferenciaMapper::toResponse);
    }

    @Override
    public Flux<TransferenciaResponseDTO> listar() {
        return transferenciaRepository.findAll()
                .map(transferenciaMapper::toResponse);
    }

    @Override
    public Flux<TransferenciaResponseDTO> consultarPorCuenta(String accountNumber) {
        return transferenciaRepository.findByCuenta(accountNumber)
                .map(transferenciaMapper::toResponse);
    }

    @Override
    public Transferencia toDomain(TransferenciaRequest request) {
        return transferenciaMapper.toDomain(request);
    }
}
