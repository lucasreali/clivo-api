package com.example.clivoapi.core.customer;

import java.util.Optional;

public record CustomerSnapshot(
        Long id,
        CustomerDetails details,
        CustomerStatus status,
        String deactivationReason,
        boolean consented) {

    public Optional<String> reasonForDeactivation() {
        return Optional.ofNullable(deactivationReason);
    }
}
