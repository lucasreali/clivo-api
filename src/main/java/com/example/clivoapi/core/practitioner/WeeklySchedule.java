package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.exception.BusinessException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record WeeklySchedule(List<AvailabilityPeriod> periods) {

    private static final Comparator<AvailabilityPeriod> IN_WEEK_ORDER =
            Comparator.comparing((AvailabilityPeriod period) -> period.weekday().value())
                    .thenComparing(period -> period.hours().start());

    public WeeklySchedule {
        periods = periods.stream().sorted(IN_WEEK_ORDER).toList();
        requireNoOverlap(periods);
    }

    public static WeeklySchedule empty() {
        return new WeeklySchedule(List.of());
    }

    public boolean covers(Weekday day, LocalTime time) {
        return periods.stream().anyMatch(period -> period.covers(day, time));
    }

    public boolean embraces(Weekday day, TimeRange hours) {
        return periods.stream().anyMatch(period -> period.embraces(day, hours));
    }

    private static void requireNoOverlap(List<AvailabilityPeriod> periods) {
        periods.stream()
                .filter(period -> collidesWithin(period, periods))
                .findFirst()
                .ifPresent(WeeklySchedule::refuseOverlap);
    }

    private static boolean collidesWithin(AvailabilityPeriod period, List<AvailabilityPeriod> periods) {
        return periods.stream().filter(other -> other != period).anyMatch(period::overlaps);
    }

    private static void refuseOverlap(AvailabilityPeriod period) {
        throw new BusinessException("availability %s overlaps another period of the same practitioner".formatted(period));
    }
}
