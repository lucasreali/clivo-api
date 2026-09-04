package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.WeeklySchedule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

record ScheduleRequest(@NotNull List<@Valid @NotNull PeriodRequest> periods) {

    WeeklySchedule toSchedule() {
        return new WeeklySchedule(periods.stream().map(PeriodRequest::toPeriod).toList());
    }
}
