package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.RecordFilling;
import java.time.Instant;

public record EncounterSnapshot(
        Long id,
        EncounterParticipants participants,
        RecordFilling filling,
        Instant startedAt,
        Instant completedAt,
        EncounterStatus status) {

    public boolean isCompleted() {
        return status == EncounterStatus.COMPLETED;
    }
}
