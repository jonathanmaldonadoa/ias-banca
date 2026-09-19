package com.banco.ias.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_events")
public class OutboxEventEntity {
    @Id private UUID id;
    @Column("request_reference") private String requestReference;
    private String status;
    private int attempts;
    @Column("available_at") private OffsetDateTime availableAt;
    @Column("published_at") private OffsetDateTime publishedAt;
    @Column("created_at") private OffsetDateTime createdAt;

    public OutboxEventEntity() { }

    public OutboxEventEntity(UUID id, String requestReference, String status, int attempts,
                             OffsetDateTime availableAt, OffsetDateTime publishedAt,
                             OffsetDateTime createdAt) {
        this.id = id;
        this.requestReference = requestReference;
        this.status = status;
        this.attempts = attempts;
        this.availableAt = availableAt;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getRequestReference() { return requestReference; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public OffsetDateTime getAvailableAt() { return availableAt; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
