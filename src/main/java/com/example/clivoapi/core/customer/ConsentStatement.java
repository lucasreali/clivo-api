package com.example.clivoapi.core.customer;

public record ConsentStatement(ConsentPurpose purpose, boolean granted, String source) {

    public ConsentStatement {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("a consent needs the source it was collected from");
        }
    }
}
