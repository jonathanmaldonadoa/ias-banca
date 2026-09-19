package com.banco.ias.persistence.local;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.banco.ias.business.mapper.TransferenciaMapper;
import com.banco.ias.domain.model.EstadoTransferencia;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.TransferenciaRepository;
import com.banco.ias.persistence.entity.TransferenciaEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryTransferenciaRepository implements TransferenciaRepository {

    private final Map<UUID, TransferenciaEntity> store = new ConcurrentHashMap<>();
    private final Map<String, TransferenciaEntity> byRequestReference = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> acumuladoDiarioPorCuenta = new ConcurrentHashMap<>();
    private final TransferenciaMapper transferenciaMapper;

    public InMemoryTransferenciaRepository(TransferenciaMapper transferenciaMapper) {
        this.transferenciaMapper = transferenciaMapper;
    }

    @Override
    public Mono<Transferencia> save(Transferencia transferencia) {
        if (transferencia == null) {
            return Mono.empty();
        }

        if (transferencia.requestReference() != null && !transferencia.requestReference().isBlank()) {
            TransferenciaEntity existente = byRequestReference.get(transferencia.requestReference());
            if (existente != null) {
                return Mono.just(transferenciaMapper.toDomain(existente));
            }
        }

        if (transferencia.estado() == EstadoTransferencia.APROBADA) {
            String sourceAccountId = transferencia.sourceAccountId();
            BigDecimal acumuladoActual = acumuladoDiarioPorCuenta.getOrDefault(sourceAccountId, BigDecimal.ZERO);
            BigDecimal nuevoAcumulado = acumuladoActual.add(transferencia.amount());
            acumuladoDiarioPorCuenta.put(sourceAccountId, nuevoAcumulado);
        }

        TransferenciaEntity entity = transferenciaMapper.toEntity(transferencia);
        store.put(entity.getId(), entity);
        byRequestReference.put(entity.getRequestReference(), entity);
        return Mono.just(transferenciaMapper.toDomain(entity));
    }

    @Override
    public Mono<Transferencia> findByRequestReference(String requestReference) {
        return Mono.justOrEmpty(byRequestReference.get(requestReference))
                .map(transferenciaMapper::toDomain);
    }

    @Override
    public Flux<Transferencia> findAll() {
        return Flux.fromIterable(store.values())
                .sort(Comparator.comparing(TransferenciaEntity::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(transferenciaMapper::toDomain);
    }

    @Override
    public Flux<Transferencia> findRecentProcessed() {
        return Flux.fromIterable(store.values())
                .filter(entity -> entity.getEstado() != null
                        && !"PENDIENTE".equals(entity.getEstado()))
                .sort(Comparator.comparing(TransferenciaEntity::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(transferenciaMapper::toDomain);
    }

    @Override
    public Flux<Transferencia> findByCuenta(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return Flux.empty();
        }

        return Flux.fromIterable(store.values())
                .filter(entity -> accountNumber.equals(entity.getSourceAccountId())
                        || accountNumber.equals(entity.getDestinationAccountId()))
                .sort(Comparator.comparing(TransferenciaEntity::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(transferenciaMapper::toDomain);
    }

    @Override
    public Mono<BigDecimal> findAcumuladoDiarioPorCuenta(String sourceAccountId) {
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            return Mono.just(BigDecimal.ZERO);
        }

        return Mono.just(acumuladoDiarioPorCuenta.getOrDefault(sourceAccountId, BigDecimal.ZERO));
    }
}
