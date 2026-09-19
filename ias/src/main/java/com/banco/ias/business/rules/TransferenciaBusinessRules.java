package com.banco.ias.business.rules;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.banco.ias.core.exception.BusinessRuleException;
import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.model.Transferencia;
import com.banco.ias.domain.repository.CuentaRepository;

@Component
public class TransferenciaBusinessRules {

    private final CuentaRepository cuentaRepository;

    public TransferenciaBusinessRules(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public void validarAntesDeRegistrar(Transferencia transferencia) {
        validarRF01(transferencia);
        validarRF02(transferencia);
    }

    public void validarRF01(Transferencia transferencia) {
        if (transferencia == null) {
            throw new BusinessRuleException("RF01: La transferencia es obligatoria");
        }

        if (transferencia.requestReference() == null || transferencia.requestReference().isBlank()) {
            throw new BusinessRuleException("RF01: La referencia de solicitud es obligatoria");
        }

        validarCuentaOrigenYDestino(transferencia, "RF01");

        if (transferencia.currency() == null || transferencia.currency().isBlank()) {
            throw new BusinessRuleException("RF01: La moneda es obligatoria");
        }

        if (transferencia.createdAt() == null) {
            throw new BusinessRuleException("RF01: La fecha y hora de procesamiento es obligatoria");
        }
    }

    public void validarRF02(Transferencia transferencia) {
        if (transferencia == null) {
            throw new BusinessRuleException("RF02: La transferencia es obligatoria");
        }

        validarCuentaOrigenYDestino(transferencia, "RF02");

        if (transferencia.sourceAccountId().equals(transferencia.destinationAccountId())) {
            throw new BusinessRuleException("RF02: La cuenta origen y destino deben ser diferentes");
        }

        Cuenta origen = cuentaRepository.findByNumero(transferencia.sourceAccountId());

        if (transferencia.amount().compareTo(origen.limiteDiario()) > 0) {
            throw new BusinessRuleException(
                    "RF02: La operación supera el límite diario permitido para la cuenta origen: "
                            + origen.limiteDiario() + " " + origen.moneda()
            );
        }
    }

    public void validarRF03(Transferencia transferencia) {
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

        if (transferencia.estado() == com.banco.ias.domain.model.EstadoTransferencia.RECHAZADA) {
            return;
        }

        if (transferencia.estado() == com.banco.ias.domain.model.EstadoTransferencia.APROBADA) {
            validarCuentaOrigenYDestino(transferencia, "RF03");
        }
    }

    public void validarRF04(String sourceAccountId, BigDecimal montoSolicitado, BigDecimal acumuladoDiarioActual) {
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new BusinessRuleException("RF04: La cuenta origen es obligatoria");
        }

        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("RF04: El monto debe ser mayor que cero");
        }

        if (acumuladoDiarioActual == null) {
            acumuladoDiarioActual = BigDecimal.ZERO;
        }

        Cuenta origen = cuentaRepository.findByNumero(sourceAccountId);
        if (origen == null) {
            throw new BusinessRuleException("RF04: La cuenta origen no existe: " + sourceAccountId);
        }

        BigDecimal nuevoTotal = acumuladoDiarioActual.add(montoSolicitado);
        if (nuevoTotal.compareTo(origen.limiteDiario()) > 0) {
            throw new BusinessRuleException(
                    "RF04: La operación no puede procesarse porque excede el límite diario de la cuenta origen: "
                            + origen.limiteDiario() + " " + origen.moneda()
            );
        }
    }

    public void validarRF05(String requestReference, Transferencia transferenciaExistente) {
        if (requestReference == null || requestReference.isBlank()) {
            throw new BusinessRuleException("RF05: La referencia de solicitud es obligatoria");
        }

        if (transferenciaExistente != null) {
            throw new BusinessRuleException(
                    "RF05: La requestReference ya existe en el sistema: " + requestReference
            );
        }
    }

    public void validarRF08(Transferencia transferencia) {
        if (transferencia == null) {
            throw new BusinessRuleException("RF08: La transferencia es obligatoria");
        }

        validarCuentaOrigenYDestino(transferencia, "RF08");

        Cuenta origen = cuentaRepository.findByNumero(transferencia.sourceAccountId());
        if (origen.saldo().compareTo(transferencia.amount()) < 0) {
            throw new BusinessRuleException(
                    "RF08: La cuenta origen no tiene saldo suficiente para realizar la transferencia. Saldo actual: "
                            + origen.saldo() + " " + origen.moneda()
            );
        }
    }

    private void validarCuentaOrigenYDestino(Transferencia transferencia, String regla) {
        if (transferencia.sourceAccountId() == null || transferencia.sourceAccountId().isBlank()) {
            throw new BusinessRuleException(regla + ": La cuenta origen es obligatoria");
        }

        if (transferencia.destinationAccountId() == null || transferencia.destinationAccountId().isBlank()) {
            throw new BusinessRuleException(regla + ": La cuenta destino es obligatoria");
        }

        if (transferencia.amount() == null || transferencia.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(regla + ": El monto debe ser mayor que cero");
        }

        Cuenta origenExistente = cuentaRepository.findByNumero(transferencia.sourceAccountId());
        if (origenExistente == null) {
            throw new BusinessRuleException(regla + ": La cuenta origen no existe: " + transferencia.sourceAccountId());
        }

        Cuenta destinoExistente = cuentaRepository.findByNumero(transferencia.destinationAccountId());
        if (destinoExistente == null) {
            throw new BusinessRuleException(regla + ": La cuenta destino no existe: " + transferencia.destinationAccountId());
        }
    }
}
