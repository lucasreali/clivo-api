package com.example.clivoapi.modules.dependent;

import java.util.Optional;
import java.util.UUID;

public record DependentSnapshot(
        UUID id, UUID customerId, DependentDetails details, DependentStatus status, Integer age) {

    public Optional<Integer> ageInYears() {
        return Optional.ofNullable(age);
    }
}
