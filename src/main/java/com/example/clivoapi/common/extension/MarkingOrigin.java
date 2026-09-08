package com.example.clivoapi.common.extension;

import java.time.Instant;
import java.util.UUID;

public record MarkingOrigin(
        MarkingSource source, Instant since, Instant recordedAt, UUID encounterId, String practitionerName) {

    public MarkingOrigin carriedFrom(MarkingOrigin earlier) {
        return new MarkingOrigin(source, earlier.since(), recordedAt, encounterId, practitionerName);
    }
}
