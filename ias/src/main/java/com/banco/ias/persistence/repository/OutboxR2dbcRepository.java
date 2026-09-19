package com.banco.ias.persistence.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.banco.ias.persistence.entity.OutboxEventEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxR2dbcRepository extends ReactiveCrudRepository<OutboxEventEntity, UUID> {
    @Query("SELECT * FROM outbox_events WHERE status = 'PENDING' "
            + "AND available_at <= :now ORDER BY created_at LIMIT 50")
    Flux<OutboxEventEntity> findPending(OffsetDateTime now);

    @Query("UPDATE outbox_events SET status = 'PUBLISHED', published_at = :publishedAt "
            + "WHERE id = :id AND status = 'PENDING'")
        Mono<Integer> markPublished(UUID id, OffsetDateTime publishedAt);

        @Query("UPDATE outbox_events SET attempts = attempts + 1, "
                        + "available_at = :availableAt WHERE id = :id AND status = 'PENDING'")
        Mono<Integer> registerFailure(UUID id, OffsetDateTime availableAt);
}
