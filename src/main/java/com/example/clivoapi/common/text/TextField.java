package com.example.clivoapi.common.text;

import com.example.clivoapi.common.exception.BusinessException;

public record TextField(String name, int maximumLength) {

    public String required(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("%s is required".formatted(name));
        }
        return withinLength(value.trim());
    }

    public String optional(String value) {
        return value == null || value.isBlank() ? null : withinLength(value.trim());
    }

    private String withinLength(String value) {
        if (value.length() <= maximumLength) {
            return value;
        }
        throw new BusinessException("%s is limited to %d characters".formatted(name, maximumLength));
    }
}
