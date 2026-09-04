package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.ScheduleBlockSnapshot;
import java.time.LocalDateTime;

record ScheduleBlockView(
        Long id,
        Long practitionerId,
        boolean clinicWide,
        LocalDateTime start,
        LocalDateTime end,
        String reason) {

    static ScheduleBlockView of(ScheduleBlockSnapshot block) {
        TimeWindow period = block.period();
        return new ScheduleBlockView(
                block.id(),
                block.practitionerId(),
                block.isClinicWide(),
                period.start(),
                period.end(),
                block.reason());
    }
}
