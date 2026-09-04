package com.example.clivoapi.core.scheduling;

import java.time.LocalDateTime;

public record AppointmentBooking(Long customerId, Long practitionerId, Long serviceId, LocalDateTime start) {
}
