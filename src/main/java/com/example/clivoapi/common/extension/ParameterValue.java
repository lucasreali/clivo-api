package com.example.clivoapi.common.extension;

import java.math.BigDecimal;

public record ParameterValue(String value) {

    public ParameterValue {
        if (value == null) {
            throw new IllegalArgumentException("a parameter value is required");
        }
    }

    public static ParameterValue of(String value) {
        return new ParameterValue(value);
    }

    public String asText() {
        return value;
    }

    public int asInteger() {
        return asDecimal().intValueExact();
    }

    public BigDecimal asDecimal() {
        return new BigDecimal(value.trim());
    }

    public boolean asFlag() {
        return Boolean.parseBoolean(value.trim());
    }

    public boolean isBlank() {
        return value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
