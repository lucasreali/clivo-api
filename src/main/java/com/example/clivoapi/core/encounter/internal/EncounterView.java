package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.core.encounter.EncounterParticipants;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import java.time.Instant;

record EncounterView(
        Long id,
        Long appointmentId,
        Long customerId,
        String customerName,
        Long practitionerId,
        String practitionerName,
        Long serviceId,
        String serviceName,
        RecordSheet sheet,
        Instant startedAt,
        Instant completedAt,
        String status) {

    static EncounterView of(EncounterSnapshot encounter) {
        EncounterParticipants participants = encounter.participants();
        return new EncounterView(
                encounter.id(),
                participants.appointmentId(),
                participants.customerId(),
                participants.customerName(),
                participants.practitionerId(),
                participants.practitionerName(),
                participants.serviceId(),
                participants.serviceName(),
                encounter.sheet(),
                encounter.startedAt(),
                encounter.completedAt(),
                encounter.status().name());
    }
}
