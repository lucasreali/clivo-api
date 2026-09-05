package com.example.clivoapi.modules.notification;

import com.example.clivoapi.common.exception.BusinessException;

public record Reply(String value) {

    private static final int MAXIMUM_LENGTH = 20;

    public Reply {
        value = answered(value);
    }

    public String asText() {
        return value;
    }

    private static String answered(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("an answer without content is not a reply");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a reply is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
