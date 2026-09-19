package com.banco.ias.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.ConnectionFactory;

import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

@Configuration
public class RabbitMqConfig {

    @Bean
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${ias.rabbitmq.host}") String host,
            @Value("${ias.rabbitmq.port}") int port,
            @Value("${ias.rabbitmq.username}") String username,
            @Value("${ias.rabbitmq.password}") String password) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public Sender rabbitSender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(new SenderOptions().connectionFactory(connectionFactory));
    }

    @Bean
    public Receiver rabbitReceiver(ConnectionFactory connectionFactory) {
        return RabbitFlux.createReceiver(new reactor.rabbitmq.ReceiverOptions()
                .connectionFactory(connectionFactory));
    }
}
