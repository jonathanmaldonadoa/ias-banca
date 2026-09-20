package com.banco.ias.business.service;

import com.banco.ias.domain.model.Cuenta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CuentaService {
    Flux<Cuenta> listar();
    Mono<Cuenta> obtenerPorNumero(String numero);
    Mono<Cuenta> findByNumero(String numero);
}
