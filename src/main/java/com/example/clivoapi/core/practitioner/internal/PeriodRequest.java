package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.AvailabilityPeriod;
import com.example.clivoapi.core.practitioner.TimeRange;
import com.example.clivoapi.core.practitioner.Weekday;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

record PeriodRequest(@NotNull DayOfWeek weekday, @NotNull LocalTime start, @NotNull LocalTime end) {

    AvailabilityPeriod toPeriod() {
        return new AvailabilityPeriod(Weekday.of(weekday), new TimeRange(start, end));
    }
}
