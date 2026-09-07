package com.example.clivoapi.core.clinical.internal;

import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.time.Instant;
import java.util.UUID;

record AlertView(UUID id, UUID customerId, String note, UUID authorId, String authorName, Instant recordedAt) {

    static AlertView of(ClinicalAlertSnapshot alert) {
        return new AlertView(
                alert.id(),
                alert.customerId(),
                alert.note(),
                alert.authorId(),
                alert.authorName(),
                alert.recordedAt());
    }
}
