package com.example.clivoapi.core.practitioner;

import java.time.LocalTime;

public record AvailabilityPeriod(Weekday weekday, TimeRange hours) {

    public boolean overlaps(AvailabilityPeriod other) {
        return weekday.equals(other.weekday) && hours.overlaps(other.hours);
    }

    public boolean covers(Weekday day, LocalTime time) {
        return weekday.equals(day) && hours.covers(time);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(weekday, hours);
    }
}
