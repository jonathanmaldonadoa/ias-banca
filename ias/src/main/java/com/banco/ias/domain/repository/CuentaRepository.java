package com.banco.ias.domain.repository;

import com.banco.ias.domain.model.Cuenta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CuentaRepository {
    Flux<Cuenta> findAll();
    Mono<Cuenta> findByNumero(String numero);
    Mono<Cuenta> save(Cuenta cuenta);
}
