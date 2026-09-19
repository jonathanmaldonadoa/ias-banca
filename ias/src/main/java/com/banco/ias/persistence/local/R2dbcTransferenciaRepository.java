package com.banco.ias.persistence.local;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Repository;

import com.banco.ias.business.mapper.TransferenciaMapper;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.TransferenciaRepository;
import com.banco.ias.persistence.repository.TransferenciaR2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcTransferenciaRepository implements TransferenciaRepository {
    private final TransferenciaR2dbcRepository repository;
    private final TransferenciaMapper mapper;

    public R2dbcTransferenciaRepository(TransferenciaR2dbcRepository repository,
                                        TransferenciaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Transferencia> save(Transferencia transferencia) {
        var entity = mapper.toEntity(transferencia);
        return repository.save(entity).map(mapper::toDomain);
    }

    @Override
    public Mono<Transferencia> insertIfAbsent(Transferencia transferencia) {
        var entity = mapper.toEntity(transferencia);
        return repository.insertIfAbsent(entity.getId(), entity.getRequestReference(), entity.getSourceAccountId(),
                entity.getDestinationAccountId(), entity.getAmount(), entity.getCurrency(), entity.getEstado(),
                entity.getResultado(), entity.getObservacion(), entity.getCreatedAt(), entity.getUpdatedAt())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Transferencia> findByRequestReference(String requestReference) {
        return repository.findByRequestReference(requestReference).map(mapper::toDomain);
    }

    @Override
    public Flux<Transferencia> findAll() {
        return repository.findAllByOrderByUpdatedAtDesc().map(mapper::toDomain);
    }

    @Override
    public Flux<Transferencia> findRecentProcessed() {
        return findAll().filter(transferencia -> transferencia.estado() != null
                && transferencia.estado() != com.banco.ias.domain.model.EstadoTransferencia.PENDIENTE);
    }

    @Override
    public Flux<Transferencia> findByCuenta(String accountNumber) {
        return repository.findBySourceAccountIdOrDestinationAccountIdOrderByUpdatedAtDesc(
                accountNumber, accountNumber).map(mapper::toDomain);
    }

    @Override
    public Mono<BigDecimal> findAcumuladoDiarioPorCuenta(String sourceAccountId) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime from = now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        return repository.sumApprovedAmountByAccountAndPeriod(sourceAccountId, from, from.plusDays(1))
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
