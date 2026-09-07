package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.AppointmentBooking;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

record AppointmentRequest(
        @NotNull UUID customerId,
        @NotNull UUID practitionerId,
        @NotNull UUID serviceId,
        @NotNull LocalDateTime start) {

    AppointmentBooking toBooking() {
        return new AppointmentBooking(customerId, practitionerId, serviceId, start);
    }
}
