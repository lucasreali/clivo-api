package com.example.clivoapi.core.access;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern SHAPE = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public EmailAddress {
        value = normalized(value);
    }

    public String asText() {
        return value;
    }

    private static String normalized(String value) {
        if (value != null && SHAPE.matcher(value.trim()).matches()) {
            return value.trim().toLowerCase(Locale.ROOT);
        }
        throw new IllegalArgumentException("a valid email address is required");
    }

    @Override
    public String toString() {
        return value;
    }
}
