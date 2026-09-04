package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.Optional;

public record ScheduleBlockSnapshot(Long id, Long practitionerId, TimeWindow period, String reason) {

    public Optional<Long> practitioner() {
        return Optional.ofNullable(practitionerId);
    }

    public boolean isClinicWide() {
        return practitionerId == null;
    }
}
