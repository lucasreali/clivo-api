package com.example.clivoapi.common.extension;

public record ParameterCode(String value) {

    public ParameterCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a parameter code is required");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
