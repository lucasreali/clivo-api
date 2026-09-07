package com.example.clivoapi.common.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEntrySnapshot(
        UUID author,
        Instant occurredAt,
        AuditAction action,
        Map<String, Object> previousValue,
        Map<String, Object> newValue) {
}
