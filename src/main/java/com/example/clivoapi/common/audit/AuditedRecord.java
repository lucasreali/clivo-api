package com.example.clivoapi.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class AuditedRecord {

    @Column(name = "entity", nullable = false, updatable = false)
    private String entity;

    @Column(name = "record_id", nullable = false, updatable = false)
    private UUID identifier;

    protected AuditedRecord() {
    }

    public AuditedRecord(String entity, UUID identifier) {
        requireIdentified(entity, identifier);
        this.entity = entity;
        this.identifier = identifier;
    }

    private static void requireIdentified(String entity, UUID identifier) {
        if (entity == null || entity.isBlank() || identifier == null) {
            throw new IllegalArgumentException("an audited record is an entity name and a record identifier");
        }
    }
}
