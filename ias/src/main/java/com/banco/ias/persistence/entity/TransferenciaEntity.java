package com.banco.ias.persistence.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("transferencias")
public class TransferenciaEntity {
    @Id private UUID id;
    @Column("request_reference") private String requestReference;
    @Column("source_account_id") private String sourceAccountId;
    @Column("destination_account_id") private String destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String estado;
    private String resultado;
    private String observacion;
    @Column("created_at") private OffsetDateTime createdAt;
    @Column("updated_at") private OffsetDateTime updatedAt;

    public TransferenciaEntity() { }

    public TransferenciaEntity(UUID id, String requestReference, String sourceAccountId,
                               String destinationAccountId, BigDecimal amount, String currency,
                               String estado, String resultado, String observacion,
                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.requestReference = requestReference;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.estado = estado;
        this.resultado = resultado;
        this.observacion = observacion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public String getRequestReference() { return requestReference; }
    public void setRequestReference(String value) { requestReference = value; }
    public String getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(String value) { sourceAccountId = value; }
    public String getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(String value) { destinationAccountId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String value) { currency = value; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { estado = value; }
    public String getResultado() { return resultado; }
    public void setResultado(String value) { resultado = value; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String value) { observacion = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime value) { createdAt = value; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime value) { updatedAt = value; }
}
