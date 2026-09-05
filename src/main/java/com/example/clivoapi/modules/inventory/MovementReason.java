package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;

public record MovementReason(String value) {

    private static final int MAXIMUM_LENGTH = 160;

    public MovementReason {
        value = stated(value);
    }

    public String asText() {
        return value;
    }

    private static String stated(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a stock movement is recorded with its reason");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a movement reason is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
