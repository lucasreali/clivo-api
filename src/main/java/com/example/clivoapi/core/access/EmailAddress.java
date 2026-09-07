package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.regex.Pattern;

@Embeddable
public record EmailAddress(@Column(name = "email", length = 160) String value) {

    private static final Pattern SHAPE = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private static final int MAXIMUM_LENGTH = 160;

    public EmailAddress {
        value = normalized(value);
    }

    public String asText() {
        return value;
    }

    private static String normalized(String value) {
        String candidate = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        requireShape(candidate);
        requireLength(candidate);
        return candidate;
    }

    private static void requireShape(String candidate) {
        if (SHAPE.matcher(candidate).matches()) {
            return;
        }
        throw new BusinessException("email: a valid email address is required");
    }

    private static void requireLength(String candidate) {
        if (candidate.length() <= MAXIMUM_LENGTH) {
            return;
        }
        throw new BusinessException("email: limited to %d characters".formatted(MAXIMUM_LENGTH));
    }

    @Override
    public String toString() {
        return value;
    }
}
