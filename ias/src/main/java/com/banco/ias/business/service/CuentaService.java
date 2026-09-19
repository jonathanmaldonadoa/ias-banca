package com.banco.ias.business.service;

import java.util.List;

import com.banco.ias.domain.model.Cuenta;

public interface CuentaService {
    List<Cuenta> listar();
    Cuenta obtenerPorNumero(String numero);
    Cuenta findByNumero(String numero);
}
