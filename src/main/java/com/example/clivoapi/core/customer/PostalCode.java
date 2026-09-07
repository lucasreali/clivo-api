package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.document.Digits;
import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PostalCode(@Column(name = "postal_code", length = 8) String value) {

    private static final int LENGTH = 8;

    public PostalCode {
        value = verified(Digits.of(value));
    }

    public String asText() {
        return value;
    }

    private static String verified(Digits digits) {
        if (digits.hasLength(LENGTH)) {
            return digits.asText();
        }
        throw new BusinessException("postalCode: a postal code holds %d digits".formatted(LENGTH));
    }

    @Override
    public String toString() {
        return value;
    }
}
