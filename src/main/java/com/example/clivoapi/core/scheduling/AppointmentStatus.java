package com.example.clivoapi.core.scheduling;

import java.util.EnumSet;
import java.util.Set;

public enum AppointmentStatus {

    SCHEDULED,
    CONFIRMED,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    private static final Set<AppointmentStatus> OCCUPYING =
            EnumSet.of(SCHEDULED, CONFIRMED, ARRIVED, IN_PROGRESS);

    private static final Set<AppointmentStatus> AWAITING_ARRIVAL = EnumSet.of(SCHEDULED, CONFIRMED);

    public boolean occupiesAgenda() {
        return OCCUPYING.contains(this);
    }

    public boolean awaitsArrival() {
        return AWAITING_ARRIVAL.contains(this);
    }

    public boolean isWaiting() {
        return this == ARRIVED;
    }
}
