package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.RecordSheet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record EncounterSnapshot(
        UUID id,
        EncounterParticipants participants,
        RecordSheet sheet,
        Instant startedAt,
        Instant completedAt,
        EncounterStatus status) {

    public boolean isCompleted() {
        return status == EncounterStatus.COMPLETED;
    }

    public Optional<RecordSheet> clinicalRecord() {
        return Optional.ofNullable(sheet);
    }
}
