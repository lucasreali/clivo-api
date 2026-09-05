package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record MemberNumber(@Column(name = "member_number", nullable = false, length = 40) String value) {

    private static final int MAXIMUM_LENGTH = 40;

    public MemberNumber {
        value = printed(value);
    }

    public String asText() {
        return value;
    }

    private static String printed(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a membership carries the number printed on the card");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a member number is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
