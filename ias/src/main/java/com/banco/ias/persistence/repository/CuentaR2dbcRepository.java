package com.banco.ias.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.banco.ias.persistence.entity.CuentaEntity;

import reactor.core.publisher.Mono;

public interface CuentaR2dbcRepository extends ReactiveCrudRepository<CuentaEntity, UUID> {
    Mono<CuentaEntity> findByNumero(String numero);
}
