package com.banco.ias.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cuentas")
public class CuentaEntity {
    @Id private UUID id;
    private String numero;
    private String condicion;
    @Column("limite_diario") private BigDecimal limiteDiario;
    private String moneda;
    private BigDecimal saldo;

    public CuentaEntity() { }

    public CuentaEntity(UUID id, String numero, String condicion, BigDecimal limiteDiario,
                        String moneda, BigDecimal saldo) {
        this.id = id;
        this.numero = numero;
        this.condicion = condicion;
        this.limiteDiario = limiteDiario;
        this.moneda = moneda;
        this.saldo = saldo;
    }

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public String getNumero() { return numero; }
    public void setNumero(String value) { numero = value; }
    public String getCondicion() { return condicion; }
    public void setCondicion(String value) { condicion = value; }
    public BigDecimal getLimiteDiario() { return limiteDiario; }
    public void setLimiteDiario(BigDecimal value) { limiteDiario = value; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String value) { moneda = value; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal value) { saldo = value; }
}
