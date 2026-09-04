package com.example.clivoapi.core.practitioner;

import java.time.DayOfWeek;

public record Weekday(short value) {

    private static final short FIRST = 0;
    private static final short LAST = 6;

    public Weekday {
        if (value < FIRST || value > LAST) {
            throw new IllegalArgumentException("a weekday goes from %d (Monday) to %d (Sunday)".formatted(FIRST, LAST));
        }
    }

    public static Weekday of(DayOfWeek day) {
        return new Weekday((short) (day.getValue() - 1));
    }

    public DayOfWeek asDayOfWeek() {
        return DayOfWeek.of(value + 1);
    }

    @Override
    public String toString() {
        return asDayOfWeek().name();
    }
}
