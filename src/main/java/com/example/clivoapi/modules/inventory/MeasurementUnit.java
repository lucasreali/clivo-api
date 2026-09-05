package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record MeasurementUnit(@Column(name = "unit", nullable = false, length = 10) String value) {

    private static final int MAXIMUM_LENGTH = 10;

    public MeasurementUnit {
        value = abbreviated(value);
    }

    public String asText() {
        return value;
    }

    private static String abbreviated(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a product is measured in some unit");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a unit is abbreviated to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
