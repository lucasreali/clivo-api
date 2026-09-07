package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CoverageNote;
import com.example.clivoapi.common.extension.EncounterCharge;
import java.util.List;
import java.util.Optional;

public record HistoryEntry(
        EncounterSnapshot encounter,
        EncounterCharge charge,
        CoverageNote insurance,
        List<AttachmentSnapshot> attachments) {

    public boolean hasStatus(EncounterStatus status) {
        return encounter.status() == status;
    }

    public Optional<CoverageNote> coveredBy() {
        return Optional.ofNullable(insurance);
    }

    public Optional<List<AttachmentSnapshot>> files() {
        return Optional.ofNullable(attachments);
    }
}
