package com.example.clivoapi.common.extension;

public record CoverageNote(String plan, String memberNumber) {

    public CoverageNote {
        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("a coverage note names who covers the customer");
        }
    }
}
