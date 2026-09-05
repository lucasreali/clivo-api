package com.example.clivoapi.modules.batch;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BatchCode(@Column(name = "code", nullable = false, length = 40) String value) {

    private static final int MAXIMUM_LENGTH = 40;

    public BatchCode {
        value = printed(value);
    }

    public String asText() {
        return value;
    }

    private static String printed(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a batch carries the code printed on its package");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a batch code is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
