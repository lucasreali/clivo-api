package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Arrays;

public enum InvoiceCoverage {

    DIRECT,
    SESSION_PACKAGE,
    INSURANCE;

    public boolean isThirdParty() {
        return this != DIRECT;
    }

    public static InvoiceCoverage of(String value) {
        return Arrays.stream(values())
                .filter(coverage -> coverage.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "an invoice is covered by one of %s".formatted(Arrays.toString(values()))));
    }
}
