package com.example.clivoapi.modules.notification;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Recipient(@Column(name = "recipient", nullable = false, length = 160) String value) {

    private static final int MAXIMUM_LENGTH = 160;

    public Recipient {
        value = reachable(value);
    }

    public String asText() {
        return value;
    }

    private static String reachable(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a reminder needs somewhere to be delivered");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a recipient is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
