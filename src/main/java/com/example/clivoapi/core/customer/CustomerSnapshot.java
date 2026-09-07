package com.example.clivoapi.core.customer;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record CustomerSnapshot(
        UUID id,
        CustomerDetails details,
        CustomerStatus status,
        String deactivationReason,
        boolean consented,
        Instant registeredAt) {

    public Optional<String> reasonForDeactivation() {
        return Optional.ofNullable(deactivationReason);
    }
}
