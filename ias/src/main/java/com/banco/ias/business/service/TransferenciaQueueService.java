package com.banco.ias.business.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;

public interface TransferenciaQueueService {

    /**
     * Publica una referencia de transferencia en la cola configurada.
     *
     * @param requestReference referencia de la transferencia
     * @return operacion reactiva que finaliza cuando el mensaje fue enviado
     */
    Mono<Void> publicar(String requestReference);

    /**
     * Consume mensajes pendientes y los entrega despues de un breve retraso.
     * El consumidor utiliza confirmacion manual para evitar perder mensajes.
     *
     * @return flujo de transferencias pendientes
     */
    Flux<PendingTransfer> mensajes();

    record PendingTransfer(String requestReference, AcknowledgableDelivery delivery) {

        /**
         * Confirma manualmente que el mensaje fue procesado.
         */
        public void ack() {
            delivery.ack();
        }
    }
}
