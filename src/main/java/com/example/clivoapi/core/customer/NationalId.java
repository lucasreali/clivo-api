package com.example.clivoapi.core.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public record NationalId(@Column(name = "national_id", length = 11) String value) {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("\\d{11}");

    public NationalId {
        value = normalized(value);
    }

    public String asText() {
        return value;
    }

    private static String normalized(String value) {
        String digits = String.valueOf(value).replaceAll("\\D", "");
        if (ELEVEN_DIGITS.matcher(digits).matches()) {
            return digits;
        }
        throw new IllegalArgumentException("a national id has 11 digits");
    }

    @Override
    public String toString() {
        return value;
    }
}
