package com.example.clivoapi.core.practitioner.internal;

import java.time.DayOfWeek;
import java.time.LocalTime;

record AttendanceView(String weekday, LocalTime time, boolean works) {

    static AttendanceView of(DayOfWeek weekday, LocalTime time, boolean works) {
        return new AttendanceView(weekday.name(), time, works);
    }
}
