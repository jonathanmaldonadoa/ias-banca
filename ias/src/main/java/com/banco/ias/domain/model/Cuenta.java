package com.banco.ias.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Cuenta(
        UUID id,
        String numero,
        String condicion,
        BigDecimal limiteDiario,
        String moneda,
        BigDecimal saldo
) {
    public Cuenta {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de cuenta es obligatorio");
        }
        if (condicion == null || condicion.isBlank()) {
            throw new IllegalArgumentException("La condición es obligatoria");
        }
        if (limiteDiario == null || limiteDiario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El límite diario debe ser mayor a cero");
        }
        if (moneda == null || moneda.isBlank()) {
            moneda = "COP";
        }
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
    }

    public Cuenta debitar(BigDecimal monto) {
        return new Cuenta(id, numero, condicion, limiteDiario, moneda, saldo.subtract(monto));
    }

    public Cuenta acreditar(BigDecimal monto) {
        return new Cuenta(id, numero, condicion, limiteDiario, moneda, saldo.add(monto));
    }
}
