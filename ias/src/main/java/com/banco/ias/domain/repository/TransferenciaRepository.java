package com.banco.ias.domain.repository;

import java.math.BigDecimal;
import java.util.UUID;

import com.banco.ias.domain.model.Transferencia;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransferenciaRepository {

    Mono<Transferencia> save(Transferencia transferencia);

    Mono<Transferencia> insertIfAbsent(Transferencia transferencia);

    Mono<Transferencia> findByRequestReference(String requestReference);

    Flux<Transferencia> findAll();

    Flux<Transferencia> findRecentProcessed();

    Flux<Transferencia> findByCuenta(String accountNumber);

    Mono<BigDecimal> findAcumuladoDiarioPorCuenta(String sourceAccountId);
}
