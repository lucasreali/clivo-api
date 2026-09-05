package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;

public record DiscountReason(String value) {

    private static final int MAXIMUM_LENGTH = 200;

    public DiscountReason {
        value = stated(value);
    }

    public String asText() {
        return value;
    }

    private static String stated(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a reason is required to discount an invoice");
        }
        if (value.trim().length() > MAXIMUM_LENGTH) {
            throw new BusinessException("a reason is limited to %d characters".formatted(MAXIMUM_LENGTH));
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
