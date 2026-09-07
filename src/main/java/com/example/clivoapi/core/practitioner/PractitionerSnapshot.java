package com.example.clivoapi.core.practitioner;

import java.util.UUID;

public record PractitionerSnapshot(
        UUID id,
        PractitionerDetails details,
        PractitionerStatus status,
        WeeklySchedule schedule) {
}
