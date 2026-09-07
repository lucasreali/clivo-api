package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.document.Digits;
import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PhoneNumber(@Column(name = "phone", length = 20) String value) {

    private static final int SHORTEST = 10;
    private static final int LONGEST = 11;

    public PhoneNumber {
        value = verified(Digits.of(value));
    }

    public String asText() {
        return value;
    }

    private static String verified(Digits digits) {
        if (digits.hasLength(SHORTEST) || digits.hasLength(LONGEST)) {
            return digits.asText();
        }
        throw new BusinessException(
                "phone: a Brazilian phone number holds %d or %d digits including its area code"
                        .formatted(SHORTEST, LONGEST));
    }

    @Override
    public String toString() {
        return value;
    }
}
