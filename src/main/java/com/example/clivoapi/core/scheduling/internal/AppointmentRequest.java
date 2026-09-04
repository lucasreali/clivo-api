package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.AppointmentBooking;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

record AppointmentRequest(
        @NotNull Long customerId,
        @NotNull Long practitionerId,
        @NotNull Long serviceId,
        @NotNull LocalDateTime start) {

    AppointmentBooking toBooking() {
        return new AppointmentBooking(customerId, practitionerId, serviceId, start);
    }
}
