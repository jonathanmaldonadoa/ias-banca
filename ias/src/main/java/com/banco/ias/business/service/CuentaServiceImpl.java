package com.banco.ias.business.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.repository.CuentaRepository;

@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public List<Cuenta> listar() {
        return cuentaRepository.findAll();
    }

    @Override
    public Cuenta obtenerPorNumero(String numero) {
        return cuentaRepository.findByNumero(numero);
    }

    @Override
    public Cuenta findByNumero(String numero) {
        return cuentaRepository.findByNumero(numero);
    }
}
