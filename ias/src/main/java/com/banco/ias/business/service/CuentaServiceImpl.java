package com.banco.ias.business.service;

import org.springframework.stereotype.Service;

import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.repository.CuentaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public Flux<Cuenta> listar() {
        return cuentaRepository.findAll();
    }

    @Override
    public Mono<Cuenta> obtenerPorNumero(String numero) {
        return cuentaRepository.findByNumero(numero);
    }

    @Override
    public Mono<Cuenta> findByNumero(String numero) {
        return cuentaRepository.findByNumero(numero);
    }
}
