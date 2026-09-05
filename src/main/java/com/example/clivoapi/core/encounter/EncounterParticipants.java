package com.example.clivoapi.core.encounter;

public record EncounterParticipants(
        Long appointmentId,
        Long customerId,
        String customerName,
        Long practitionerId,
        String practitionerName,
        Long serviceId,
        String serviceName) {
}
