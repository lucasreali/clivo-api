package com.example.clivoapi.core.scheduling;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentBooking(UUID customerId, UUID practitionerId, UUID serviceId, LocalDateTime start) {
}
