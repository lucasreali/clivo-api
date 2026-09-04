package com.example.clivoapi.core.catalog;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public record ServiceDuration(@Column(name = "duration_min", nullable = false) short minutes) {

    private static final short SHORTEST = 5;
    private static final short LONGEST = 480;

    public ServiceDuration {
        if (minutes < SHORTEST || minutes > LONGEST) {
            throw new BusinessException("a service lasts between %d and %d minutes".formatted(SHORTEST, LONGEST));
        }
    }

    public static ServiceDuration ofMinutes(int minutes) {
        return new ServiceDuration((short) minutes);
    }

    public LocalDateTime endFrom(LocalDateTime start) {
        return start.plusMinutes(minutes);
    }

    @Override
    public String toString() {
        return "%d min".formatted(minutes);
    }
}
