package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordFilling;
import com.example.clivoapi.core.encounter.EncounterParticipants;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import java.time.Instant;
import java.util.Map;

record EncounterView(
        Long id,
        Long appointmentId,
        Long customerId,
        String customerName,
        Long practitionerId,
        String practitionerName,
        Long serviceId,
        String serviceName,
        Long recordTemplateId,
        Map<String, Object> values,
        Instant startedAt,
        Instant completedAt,
        String status) {

    static EncounterView of(EncounterSnapshot encounter) {
        EncounterParticipants participants = encounter.participants();
        RecordFilling filling = encounter.filling();
        return new EncounterView(
                encounter.id(),
                participants.appointmentId(),
                participants.customerId(),
                participants.customerName(),
                participants.practitionerId(),
                participants.practitionerName(),
                participants.serviceId(),
                participants.serviceName(),
                filling.templateId(),
                filling.values().asMap(),
                encounter.startedAt(),
                encounter.completedAt(),
                encounter.status().name());
    }
}
