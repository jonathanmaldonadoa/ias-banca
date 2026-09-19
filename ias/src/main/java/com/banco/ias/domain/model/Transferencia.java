package com.banco.ias.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Transferencia(
        UUID id,
        String requestReference,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency,
        EstadoTransferencia estado,
        String resultado,
        String observacion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public Transferencia {
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new IllegalArgumentException("La cuenta origen es obligatoria");
        }
        if (destinationAccountId == null || destinationAccountId.isBlank()) {
            throw new IllegalArgumentException("La cuenta destino es obligatoria");
        }
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("La cuenta origen y destino deben ser diferentes");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (currency == null || currency.isBlank()) {
            currency = "COP";
        }
        if (estado == null) {
            estado = EstadoTransferencia.PENDIENTE;
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    public static Transferencia crear(String requestReference, String sourceAccountId, String destinationAccountId, BigDecimal amount, String currency) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Transferencia(
                UUID.randomUUID(),
                requestReference,
                sourceAccountId,
                destinationAccountId,
                amount,
                currency,
                EstadoTransferencia.PENDIENTE,
                "PENDIENTE",
                "Transferencia registrada para validación",
                now,
                now
        );
    }

    public Transferencia conResultado(EstadoTransferencia nuevoEstado, String nuevoResultado, String nuevaObservacion) {
        return new Transferencia(
                this.id,
                this.requestReference,
                this.sourceAccountId,
                this.destinationAccountId,
                this.amount,
                this.currency,
                nuevoEstado,
                nuevoResultado,
                nuevaObservacion,
                this.createdAt,
                OffsetDateTime.now()
        );
    }
}
