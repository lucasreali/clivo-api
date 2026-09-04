package com.example.clivoapi.core.scheduling;

public record AppointmentParticipants(
        Long customerId,
        String customerName,
        Long practitionerId,
        String practitionerName,
        Long serviceId,
        String serviceName) {
}
