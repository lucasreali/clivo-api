package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.document.CheckDigits;
import com.example.clivoapi.common.document.Digits;
import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record NationalId(@Column(name = "national_id", length = 11) String value) {

    private static final int LENGTH = 11;
    private static final int BODY_LENGTH = 9;
    private static final int HIGHEST_WEIGHT = 11;

    public NationalId {
        value = verified(Digits.of(value));
    }

    public String asText() {
        return value;
    }

    private static String verified(Digits digits) {
        requireElevenDistinctDigits(digits);
        requireCheckDigits(digits);
        return digits.asText();
    }

    private static void requireElevenDistinctDigits(Digits digits) {
        if (digits.hasLength(LENGTH) && !digits.isSingleRepeatedDigit()) {
            return;
        }
        throw new BusinessException(
                "nationalId: a CPF holds %d digits and never repeats a single one".formatted(LENGTH));
    }

    private static void requireCheckDigits(Digits digits) {
        if (new CheckDigits(digits, BODY_LENGTH, HIGHEST_WEIGHT).confirmTheDocument()) {
            return;
        }
        throw new BusinessException("nationalId: the check digits of this CPF do not match");
    }

    @Override
    public String toString() {
        return value;
    }
}
