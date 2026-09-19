package com.banco.ias.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaRequest(
        String requestReference,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency
) {
}
