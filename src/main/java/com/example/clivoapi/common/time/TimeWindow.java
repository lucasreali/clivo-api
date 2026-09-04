package com.example.clivoapi.common.time;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Embeddable
public record TimeWindow(
        @Column(name = "starts_at", nullable = false) Instant startsAt,
        @Column(name = "ends_at", nullable = false) Instant endsAt) {

    private static final ZoneOffset CLINIC_OFFSET = ZoneOffset.UTC;

    public TimeWindow {
        requireOrdered(startsAt, endsAt);
    }

    public static TimeWindow of(LocalDateTime start, LocalDateTime end) {
        return new TimeWindow(momentOf(start), momentOf(end));
    }

    public static TimeWindow wholeDay(LocalDate day) {
        return of(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
    }

    public LocalDateTime start() {
        return clockTimeOf(startsAt);
    }

    public LocalDateTime end() {
        return clockTimeOf(endsAt);
    }

    public boolean covers(LocalDateTime moment) {
        Instant instant = momentOf(moment);
        return !instant.isBefore(startsAt) && instant.isBefore(endsAt);
    }

    public boolean overlaps(TimeWindow other) {
        return startsAt.isBefore(other.endsAt) && other.startsAt.isBefore(endsAt);
    }

    public boolean startsAfter(LocalDateTime moment) {
        return startsAt.isAfter(momentOf(moment));
    }

    public boolean withinOneDay() {
        return day().equals(end().toLocalDate());
    }

    public LocalDate day() {
        return start().toLocalDate();
    }

    public DayOfWeek dayOfWeek() {
        return start().getDayOfWeek();
    }

    public LocalTime startTime() {
        return start().toLocalTime();
    }

    public LocalTime endTime() {
        return end().toLocalTime();
    }

    private static Instant momentOf(LocalDateTime clockTime) {
        return clockTime.toInstant(CLINIC_OFFSET);
    }

    private static LocalDateTime clockTimeOf(Instant moment) {
        return LocalDateTime.ofInstant(moment, CLINIC_OFFSET);
    }

    private static void requireOrdered(Instant start, Instant end) {
        if (start != null && end != null && start.isBefore(end)) {
            return;
        }
        throw new BusinessException("a time window ends after it starts");
    }

    @Override
    public String toString() {
        return "%s to %s".formatted(start(), end());
    }
}
