package com.banco.ias.domain.repository;

import java.util.List;

import com.banco.ias.domain.model.Cuenta;

public interface CuentaRepository {
    List<Cuenta> findAll();
    Cuenta findByNumero(String numero);
    Cuenta save(Cuenta cuenta);
}
