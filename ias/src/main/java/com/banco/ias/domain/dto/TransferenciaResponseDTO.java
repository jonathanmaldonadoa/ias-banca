package com.banco.ias.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.banco.ias.domain.model.EstadoTransferencia;

public record TransferenciaResponseDTO(
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
}
