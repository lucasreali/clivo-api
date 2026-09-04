package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.exception.BusinessException;

public record BlockReason(String value) {

    private static final int MAXIMUM_LENGTH = 120;

    public BlockReason {
        value = stated(value);
    }

    public String asText() {
        return value;
    }

    private static String stated(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a reason is required to block the agenda");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a block reason is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
