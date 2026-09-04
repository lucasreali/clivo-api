package com.example.clivoapi.core.access;

public record RawPassword(String value) {

    private static final int MINIMUM_LENGTH = 8;

    public RawPassword {
        if (value == null || value.length() < MINIMUM_LENGTH) {
            throw new IllegalArgumentException("a password needs at least %d characters".formatted(MINIMUM_LENGTH));
        }
    }

    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return "********";
    }
}
