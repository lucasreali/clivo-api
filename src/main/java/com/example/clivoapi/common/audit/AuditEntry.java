package com.example.clivoapi.common.audit;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "audit_log")
public class AuditEntry extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Embedded
    private AuditedRecord record;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AuditAction action;

    @Embedded
    private ValueChange change;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEntry() {
    }

    public AuditEntry(UUID userId, AuditedRecord record, AuditAction action, ValueChange change) {
        this.userId = userId;
        this.record = record;
        this.action = action;
        this.change = change;
        this.occurredAt = Instant.now();
    }

    public AuditEntrySnapshot snapshot() {
        return new AuditEntrySnapshot(userId, occurredAt, action, change.previous(), change.current());
    }
}
