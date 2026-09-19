package com.banco.ias.persistence.local;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.banco.ias.domain.model.Cuenta;
import com.banco.ias.domain.repository.CuentaRepository;

@Repository
public class InMemoryCuentaRepository implements CuentaRepository {

    private final Map<String, Cuenta> cuentas = new ConcurrentHashMap<>();

    public InMemoryCuentaRepository() {
        cuentas.put("CTA-1001", new Cuenta(UUID.fromString("11111111-1111-1111-1111-111111111111"), "CTA-1001", "Cuenta válida", new BigDecimal("5000000"), "COP", new BigDecimal("2000000")));
        cuentas.put("CTA-1002", new Cuenta(UUID.fromString("22222222-2222-2222-2222-222222222222"), "CTA-1002", "Cuenta válida", new BigDecimal("5000000"), "COP", new BigDecimal("1500000")));
        cuentas.put("CTA-2001", new Cuenta(UUID.fromString("33333333-3333-3333-3333-333333333333"), "CTA-2001", "Cuenta válida", new BigDecimal("5000000"), "COP", new BigDecimal("3500000")));
    }

    @Override
    public List<Cuenta> findAll() {
        return List.copyOf(cuentas.values());
    }

    @Override
    public Cuenta findByNumero(String numero) {
        return cuentas.get(numero);
    }

    @Override
    public Cuenta save(Cuenta cuenta) {
        if (cuenta == null || cuenta.numero() == null || cuenta.numero().isBlank()) {
            return null;
        }

        cuentas.put(cuenta.numero(), cuenta);
        return cuenta;
    }
}
