package com.banco.ias.business.service;

import java.time.Duration;
import java.time.OffsetDateTime;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.banco.ias.persistence.entity.OutboxEventEntity;
import com.banco.ias.persistence.repository.OutboxR2dbcRepository;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OutboxService {
    private final OutboxR2dbcRepository repository;
    private final TransferenciaQueueService queueService;
    private Disposable publisher;

    public OutboxService(OutboxR2dbcRepository repository, TransferenciaQueueService queueService) {
        this.repository = repository;
        this.queueService = queueService;
    }

    public Mono<Void> enqueue(String requestReference) {
        OffsetDateTime now = OffsetDateTime.now();
        return repository.save(new OutboxEventEntity(null, requestReference, "PENDING", 0,
                now, null, now)).then();
    }

    @PostConstruct
    void startPublisher() {
        publisher = Flux.interval(Duration.ZERO, Duration.ofSeconds(1))
                .flatMap(ignored -> publishPending())
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    private Mono<Void> publishPending() {
        return repository.findPending(OffsetDateTime.now())
                .concatMap(this::publish)
                .then();
    }

    private Mono<Void> publish(OutboxEventEntity event) {
        return queueService.publicar(event.getRequestReference())
                .then(repository.markPublished(event.getId(), OffsetDateTime.now()))
                .then()
                .onErrorResume(error -> registerFailure(event));
    }

    private Mono<Void> registerFailure(OutboxEventEntity event) {
        long delaySeconds = Math.min(300L, 5L * (1L << Math.min(event.getAttempts(), 6)));
        return repository.registerFailure(event.getId(), OffsetDateTime.now().plusSeconds(delaySeconds))
                .then();
    }
}
