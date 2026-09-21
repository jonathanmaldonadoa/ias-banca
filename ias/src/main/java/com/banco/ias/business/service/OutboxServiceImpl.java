package com.banco.ias.business.service;

import java.time.Duration;
import java.time.OffsetDateTime;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.banco.ias.persistence.entity.OutboxEventEntity;
import com.banco.ias.persistence.repository.OutboxR2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OutboxServiceImpl implements OutboxService {
    private final OutboxR2dbcRepository repository;
    private final TransferenciaQueueService transferenciaQueueService;

    public OutboxServiceImpl(OutboxR2dbcRepository repository, TransferenciaQueueService queueService) {
        this.repository = repository;
        this.transferenciaQueueService = queueService;
    }

    @Override
    public Mono<Void> enqueue(String requestReference) {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.save(new OutboxEventEntity(null, requestReference, "PENDING", 0,
                now, null, now)).then();
    }

    @PostConstruct
    void startPublisher() {
        // Revisa periodicamente el outbox para publicar eventos pendientes.
        Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
                .flatMap(ignored -> publishPending())
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> publishPending() {
        // Mantiene el orden de publicacion de los eventos encontrados.
        return repository.findPending(OffsetDateTime.now())
                .concatMap(this::publish)
                .then();
    }

    private Mono<Void> publish(OutboxEventEntity event) {
        // Marca el evento como publicado solo despues de enviarlo a RabbitMQ.
        return transferenciaQueueService.publicar(event.getRequestReference())
                .then(repository.markPublished(event.getId(), OffsetDateTime.now()))
                .then()
                .onErrorResume(error -> registerFailure(event));
    }

    private Mono<Void> registerFailure(OutboxEventEntity event) {
        // Aplica un backoff creciente para reintentar los envios fallidos.
        long delaySeconds = Math.min(300L, 5L * (1L << Math.min(event.getAttempts(), 6)));
        return repository.registerFailure(event.getId(), OffsetDateTime.now().plusSeconds(delaySeconds))
                .then();
    }
}