package com.example.clivoapi.common.document;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record TaxId(@Column(name = "tax_id", length = 14) String value) {

    private static final int LENGTH = 14;
    private static final int BODY_LENGTH = 12;
    private static final int HIGHEST_WEIGHT = 9;

    public TaxId {
        value = verified(Digits.of(value));
    }

    public String asText() {
        return value;
    }

    private static String verified(Digits digits) {
        requireFourteenDistinctDigits(digits);
        requireCheckDigits(digits);
        return digits.asText();
    }

    private static void requireFourteenDistinctDigits(Digits digits) {
        if (digits.hasLength(LENGTH) && !digits.isSingleRepeatedDigit()) {
            return;
        }
        throw new BusinessException("taxId: a CNPJ holds %d digits and never repeats a single one".formatted(LENGTH));
    }

    private static void requireCheckDigits(Digits digits) {
        if (new CheckDigits(digits, BODY_LENGTH, HIGHEST_WEIGHT).confirmTheDocument()) {
            return;
        }
        throw new BusinessException("taxId: the check digits of this CNPJ do not match");
    }

    @Override
    public String toString() {
        return value;
    }
}
