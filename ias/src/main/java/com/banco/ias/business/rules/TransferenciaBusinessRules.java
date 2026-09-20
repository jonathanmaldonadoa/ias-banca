package com.banco.ias.business.rules;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.banco.ias.core.exception.BusinessRuleException;
import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.CuentaRepository;

import reactor.core.publisher.Mono;

@Component
public class TransferenciaBusinessRules {
    private final CuentaRepository cuentaRepository;

    public TransferenciaBusinessRules(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public Mono<Void> validarRF01(Transferencia transferencia) {
        return validarDatosBasicos(transferencia, "RF01")
                .then(validarCuentas(transferencia, "RF01"));
    }

    public Mono<Void> validarRF02(Transferencia transferencia) {
        return validarDatosBasicos(transferencia, "RF02")
                .then(validarCuentas(transferencia, "RF02"))
                .then(cuentaRepository.findByNumero(transferencia.sourceAccountId())
                        .switchIfEmpty(Mono.error(new BusinessRuleException(
                                "RF02: La cuenta origen no existe: " + transferencia.sourceAccountId())))
                        .flatMap(origen -> {
                            if (transferencia.amount().compareTo(origen.limiteDiario()) > 0) {
                                return Mono.error(new BusinessRuleException(
                                        "RF02: La operación supera el límite diario permitido para la cuenta origen: "
                                                + origen.limiteDiario() + " " + origen.moneda()));
                            }
                            return Mono.empty();
                        }));
    }

    public Mono<Void> validarRF03(Transferencia transferencia) {
        return Mono.fromRunnable(() -> {
            if (transferencia == null) {
                throw new BusinessRuleException("RF03: La transferencia es obligatoria");
            }
            if (transferencia.estado() == null) {
                throw new BusinessRuleException("RF03: El estado de la transferencia es obligatorio");
            }
            if (transferencia.resultado() == null || transferencia.resultado().isBlank()) {
                throw new BusinessRuleException("RF03: El resultado de la operación es obligatorio");
            }
            if (transferencia.observacion() == null || transferencia.observacion().isBlank()) {
                throw new BusinessRuleException("RF03: La razón general del resultado es obligatoria");
            }
            if (transferencia.createdAt() == null) {
                throw new BusinessRuleException("RF03: La fecha y hora de procesamiento es obligatoria");
            }
        });
    }

    public Mono<Void> validarRF04(String sourceAccountId, BigDecimal montoSolicitado,
                                  BigDecimal acumuladoDiarioActual) {
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            return Mono.error(new BusinessRuleException("RF04: La cuenta origen es obligatoria"));
        }
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new BusinessRuleException("RF04: El monto debe ser mayor que cero"));
        }
        BigDecimal acumulado = acumuladoDiarioActual == null ? BigDecimal.ZERO : acumuladoDiarioActual;
        return cuentaRepository.findByNumero(sourceAccountId)
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "RF04: La cuenta origen no existe: " + sourceAccountId)))
                .flatMap(origen -> acumulado.add(montoSolicitado).compareTo(origen.limiteDiario()) > 0
                        ? Mono.error(new BusinessRuleException(
                                "RF04: La operación no puede procesarse porque excede el límite diario de la cuenta origen: "
                                        + origen.limiteDiario() + " " + origen.moneda()))
                        : Mono.empty());
    }

    public void validarRF05(String requestReference, Transferencia transferenciaExistente) {
        if (requestReference == null || requestReference.isBlank()) {
            throw new BusinessRuleException("RF05: La referencia de solicitud es obligatoria");
        }
        if (transferenciaExistente != null) {
            throw new BusinessRuleException("RF05: La requestReference ya existe en el sistema: " + requestReference);
        }
    }

    public Mono<Void> validarRF08(Transferencia transferencia) {
        return validarCuentas(transferencia, "RF08")
                .then(cuentaRepository.findByNumero(transferencia.sourceAccountId())
                        .switchIfEmpty(Mono.error(new BusinessRuleException(
                                "RF08: La cuenta origen no existe: " + transferencia.sourceAccountId())))
                        .flatMap(origen -> origen.saldo().compareTo(transferencia.amount()) < 0
                                ? Mono.error(new BusinessRuleException(
                                        "RF08: La cuenta origen no tiene saldo suficiente para realizar la transferencia. "
                                                + "Saldo actual: " + origen.saldo() + " " + origen.moneda()))
                                : Mono.empty()));
    }

    private Mono<Void> validarDatosBasicos(Transferencia transferencia, String regla) {
        return Mono.fromRunnable(() -> {
            if (transferencia == null) {
                throw new BusinessRuleException(regla + ": La transferencia es obligatoria");
            }
            if (transferencia.requestReference() == null || transferencia.requestReference().isBlank()) {
                throw new BusinessRuleException(regla + ": La referencia de solicitud es obligatoria");
            }
            if (transferencia.currency() == null || transferencia.currency().isBlank()) {
                throw new BusinessRuleException(regla + ": La moneda es obligatoria");
            }
            if (transferencia.createdAt() == null) {
                throw new BusinessRuleException(regla + ": La fecha y hora de procesamiento es obligatoria");
            }
            if (transferencia.sourceAccountId() == null || transferencia.sourceAccountId().isBlank()
                    || transferencia.destinationAccountId() == null || transferencia.destinationAccountId().isBlank()) {
                throw new BusinessRuleException(regla + ": Las cuentas origen y destino son obligatorias");
            }
            if (transferencia.sourceAccountId().equals(transferencia.destinationAccountId())) {
                throw new BusinessRuleException(regla + ": La cuenta origen y destino deben ser diferentes");
            }
            if (transferencia.amount() == null || transferencia.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException(regla + ": El monto debe ser mayor que cero");
            }
        });
    }

    private Mono<Void> validarCuentas(Transferencia transferencia, String regla) {
        return cuentaRepository.findByNumero(transferencia.sourceAccountId())
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        regla + ": La cuenta origen no existe: " + transferencia.sourceAccountId())))
                .then(cuentaRepository.findByNumero(transferencia.destinationAccountId())
                        .switchIfEmpty(Mono.error(new BusinessRuleException(
                                regla + ": La cuenta destino no existe: " + transferencia.destinationAccountId())))
                        .then());
    }
}
