package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.time.Instant;
import java.util.UUID;

record StandingAlertView(UUID id, String note, UUID authorId, String authorName, Instant recordedAt) {

    static StandingAlertView of(ClinicalAlertSnapshot alert) {
        return new StandingAlertView(
                alert.id(), alert.note(), alert.authorId(), alert.authorName(), alert.recordedAt());
    }
}
