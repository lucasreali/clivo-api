package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.core.encounter.EncounterParticipants;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
record EncounterHistoryView(
        Long id,
        Long customerId,
        Long practitionerId,
        String practitionerName,
        Long serviceId,
        String serviceName,
        Instant startedAt,
        Instant completedAt,
        String status,
        RecordSheet sheet) {

    static EncounterHistoryView of(EncounterSnapshot encounter) {
        EncounterParticipants participants = encounter.participants();
        return new EncounterHistoryView(
                encounter.id(),
                participants.customerId(),
                participants.practitionerId(),
                participants.practitionerName(),
                participants.serviceId(),
                participants.serviceName(),
                encounter.startedAt(),
                encounter.completedAt(),
                encounter.status().name(),
                encounter.clinicalRecord().orElse(null));
    }
}
