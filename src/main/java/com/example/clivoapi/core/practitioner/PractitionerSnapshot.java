package com.example.clivoapi.core.practitioner;

public record PractitionerSnapshot(
        Long id,
        PractitionerDetails details,
        PractitionerStatus status,
        WeeklySchedule schedule) {
}
