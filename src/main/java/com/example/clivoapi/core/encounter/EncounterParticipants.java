package com.example.clivoapi.core.encounter;

import java.util.UUID;

public record EncounterParticipants(
        UUID appointmentId,
        UUID customerId,
        String customerName,
        UUID practitionerId,
        String practitionerName,
        UUID serviceId,
        String serviceName) {
}
