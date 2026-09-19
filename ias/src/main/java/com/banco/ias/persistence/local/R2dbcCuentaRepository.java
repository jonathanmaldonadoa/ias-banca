package com.banco.ias.persistence.local;

import org.springframework.stereotype.Repository;

import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.repository.CuentaRepository;
import com.banco.ias.persistence.entity.CuentaEntity;
import com.banco.ias.persistence.repository.CuentaR2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcCuentaRepository implements CuentaRepository {
    private final CuentaR2dbcRepository repository;

    public R2dbcCuentaRepository(CuentaR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Cuenta> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<Cuenta> findByNumero(String numero) {
        return repository.findByNumero(numero).map(this::toDomain);
    }

    @Override
    public Mono<Cuenta> save(Cuenta cuenta) {
        return repository.save(toEntity(cuenta)).map(this::toDomain);
    }

    private Cuenta toDomain(CuentaEntity entity) {
        return new Cuenta(entity.getId(), entity.getNumero(), entity.getCondicion(),
                entity.getLimiteDiario(), entity.getMoneda(), entity.getSaldo());
    }

    private CuentaEntity toEntity(Cuenta cuenta) {
        return new CuentaEntity(cuenta.id(), cuenta.numero(), cuenta.condicion(),
                cuenta.limiteDiario(), cuenta.moneda(), cuenta.saldo());
    }
}
