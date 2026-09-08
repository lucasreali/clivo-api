package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.RecordSheet;
import java.util.Optional;
import java.util.UUID;

public record EncounterSnapshot(
        UUID id,
        EncounterParticipants participants,
        ClinicalContext context,
        RecordSheet sheet,
        EncounterTiming timing,
        Signature signature,
        EncounterStatus status) {

    public boolean isCompleted() {
        return status == EncounterStatus.COMPLETED;
    }

    public Optional<RecordSheet> clinicalRecord() {
        return Optional.ofNullable(sheet);
    }

    public Optional<Signature> signedBy() {
        return Optional.ofNullable(signature);
    }
}
