package com.banco.ias.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.ias.business.service.CuentaService;
import com.banco.ias.domain.model.Cuenta;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public List<Cuenta> listar() {
        return cuentaService.listar();
    }

    @GetMapping("/{numero}")
    public Cuenta obtenerPorNumero(@PathVariable String numero) {
        return cuentaService.obtenerPorNumero(numero);
    }
}
