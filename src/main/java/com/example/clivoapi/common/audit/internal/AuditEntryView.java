package com.example.clivoapi.common.audit.internal;

import com.example.clivoapi.common.audit.AuditAction;
import com.example.clivoapi.common.audit.AuditEntrySnapshot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

record AuditEntryView(
        UUID author,
        Instant occurredAt,
        AuditAction action,
        Map<String, Object> previousValue,
        Map<String, Object> newValue) {

    static AuditEntryView of(AuditEntrySnapshot entry) {
        return new AuditEntryView(
                entry.author(), entry.occurredAt(), entry.action(), entry.previousValue(), entry.newValue());
    }
}
