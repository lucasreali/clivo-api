package com.example.clivoapi.core.clinical;

import java.time.Instant;
import java.util.UUID;

public record ClinicalAlertSnapshot(
        UUID id, UUID customerId, String note, UUID authorId, String authorName, Instant recordedAt) {
}
