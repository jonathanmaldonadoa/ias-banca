package com.banco.ias.business.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Service
public class TransferenciaQueueService {

    private final Sender sender;
    private final Receiver receiver;
    private final String queue;

    public TransferenciaQueueService(Sender sender,
                                     Receiver receiver,
                                     @Value("${ias.rabbitmq.queue}") String queue) {
        this.sender = sender;
        this.receiver = receiver;
        this.queue = queue;
    }

    public Mono<Void> publicar(String requestReference) {
        return sender.declareQueue(QueueSpecification.queue(queue).durable(true))
                .then(sender.send(Mono.fromCallable(() -> new OutboundMessage(
                        "", queue, null, requestReference.getBytes(StandardCharsets.UTF_8)))))
                .then();
    }

    public Flux<PendingTransfer> mensajes() {
        return sender.declareQueue(QueueSpecification.queue(queue).durable(true))
            .thenMany(receiver.consumeManualAck(queue))
                .flatMap(delivery -> Mono.delay(Duration.ofSeconds(5))
                        .thenReturn(delivery)
                    .map(delayedDelivery -> new PendingTransfer(
                        new String(delayedDelivery.getBody(), StandardCharsets.UTF_8), delayedDelivery)));
    }

    public record PendingTransfer(String requestReference, AcknowledgableDelivery delivery) {
        public void ack() {
            delivery.ack();
        }
    }
}
