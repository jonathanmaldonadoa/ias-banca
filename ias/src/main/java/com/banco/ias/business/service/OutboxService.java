package com.banco.ias.business.service;

import reactor.core.publisher.Mono;

public interface OutboxService {

    /**
     * Registra una referencia para su publicacion asincrona en la cola.
     *
     * @param requestReference referencia de la transferencia a encolar
     * @return operacion reactiva que finaliza al guardar el evento
     */
    Mono<Void> enqueue(String requestReference);
}
