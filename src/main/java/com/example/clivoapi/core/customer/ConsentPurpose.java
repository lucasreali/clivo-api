package com.example.clivoapi.core.customer;

import java.util.Locale;

public record ConsentPurpose(String value) {

    private static final String DATA_PROCESSING = "DATA_PROCESSING";

    public ConsentPurpose {
        value = named(value);
    }

    public static ConsentPurpose dataProcessing() {
        return new ConsentPurpose(DATA_PROCESSING);
    }

    public String asText() {
        return value;
    }

    private static String named(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a consent purpose is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
