package com.banco.ias.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.ias.business.service.CuentaService;
import com.banco.ias.domain.model.Cuenta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public Flux<Cuenta> listar() {
        return cuentaService.listar();
    }

    @GetMapping("/{numero}")
    public Mono<org.springframework.http.ResponseEntity<Cuenta>> obtenerPorNumero(@PathVariable String numero) {
        return cuentaService.obtenerPorNumero(numero)
                .map(cuenta -> org.springframework.http.ResponseEntity.ok(cuenta))
                .defaultIfEmpty(org.springframework.http.ResponseEntity.notFound().build());
    }
}
