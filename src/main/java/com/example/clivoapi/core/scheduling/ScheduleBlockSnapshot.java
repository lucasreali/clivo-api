package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.Optional;
import java.util.UUID;

public record ScheduleBlockSnapshot(UUID id, UUID practitionerId, TimeWindow period, String reason) {

    public Optional<UUID> practitioner() {
        return Optional.ofNullable(practitionerId);
    }

    public boolean isClinicWide() {
        return practitionerId == null;
    }
}
