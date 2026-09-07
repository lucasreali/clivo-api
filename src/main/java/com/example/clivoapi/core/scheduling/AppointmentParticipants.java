package com.example.clivoapi.core.scheduling;

import java.util.UUID;

public record AppointmentParticipants(
        UUID customerId,
        String customerName,
        UUID practitionerId,
        String practitionerName,
        UUID serviceId,
        String serviceName) {
}
