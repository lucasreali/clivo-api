package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.AvailabilityPeriod;
import java.time.LocalTime;

record PeriodView(String weekday, LocalTime start, LocalTime end) {

    static PeriodView of(AvailabilityPeriod period) {
        return new PeriodView(period.weekday().toString(), period.hours().start(), period.hours().end());
    }
}
