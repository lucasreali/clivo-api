package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public record TimeRange(
        @Column(name = "start_time", nullable = false) LocalTime start,
        @Column(name = "end_time", nullable = false) LocalTime end) {

    public TimeRange {
        requireOrdered(start, end);
    }

    public boolean covers(LocalTime time) {
        return !time.isBefore(start) && time.isBefore(end);
    }

    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    private static void requireOrdered(LocalTime start, LocalTime end) {
        if (start != null && end != null && start.isBefore(end)) {
            return;
        }
        throw new BusinessException("a time range ends after it starts");
    }

    @Override
    public String toString() {
        return "%s-%s".formatted(start, end);
    }
}
