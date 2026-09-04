package com.example.clivoapi.core.access;

public record HashedPassword(String value) {

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a hashed password is required");
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
