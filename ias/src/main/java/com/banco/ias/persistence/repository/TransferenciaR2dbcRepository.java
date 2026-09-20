package com.banco.ias.persistence.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.banco.ias.persistence.entity.TransferenciaEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransferenciaR2dbcRepository extends ReactiveCrudRepository<TransferenciaEntity, UUID> {
    @Query("INSERT INTO transferencias (id, request_reference, source_account_id, destination_account_id, "
            + "amount, currency, estado, resultado, observacion, created_at, updated_at) "
            + "VALUES (:id, :requestReference, :sourceAccountId, :destinationAccountId, :amount, :currency, "
            + ":estado, :resultado, :observacion, :createdAt, :updatedAt) "
            + "ON CONFLICT (request_reference) DO NOTHING RETURNING *")
    Mono<TransferenciaEntity> insertIfAbsent(
            @Param("id") UUID id,
            @Param("requestReference") String requestReference,
            @Param("sourceAccountId") String sourceAccountId,
            @Param("destinationAccountId") String destinationAccountId,
            @Param("amount") BigDecimal amount,
            @Param("currency") String currency,
            @Param("estado") String estado,
            @Param("resultado") String resultado,
            @Param("observacion") String observacion,
            @Param("createdAt") OffsetDateTime createdAt,
            @Param("updatedAt") OffsetDateTime updatedAt);

    Mono<TransferenciaEntity> findByRequestReference(String requestReference);
    Flux<TransferenciaEntity> findAllByOrderByUpdatedAtDesc();
    Flux<TransferenciaEntity> findBySourceAccountIdOrDestinationAccountIdOrderByUpdatedAtDesc(
            String sourceAccountId, String destinationAccountId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transferencias "
            + "WHERE source_account_id = :accountId AND estado = 'APROBADA' "
            + "AND created_at >= :from AND created_at < :to")
    Mono<BigDecimal> sumApprovedAmountByAccountAndPeriod(String accountId,
                                                          OffsetDateTime from,
                                                          OffsetDateTime to);
}
