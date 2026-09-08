package com.example.clivoapi.core.encounter;

import java.time.Instant;

public record EncounterTiming(Instant startedAt, Instant lastSavedAt, Instant completedAt) {
}
