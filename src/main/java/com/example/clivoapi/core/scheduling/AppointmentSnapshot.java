package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.Optional;

public record AppointmentSnapshot(
        Long id,
        AppointmentParticipants participants,
        TimeWindow period,
        AppointmentStatus status,
        String reason) {

    public Optional<String> reasonGiven() {
        return Optional.ofNullable(reason);
    }

    public boolean isWaiting() {
        return status.isWaiting();
    }
}
