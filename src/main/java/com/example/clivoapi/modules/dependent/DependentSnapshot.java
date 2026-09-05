package com.example.clivoapi.modules.dependent;

import java.util.Optional;

public record DependentSnapshot(
        Long id, Long customerId, DependentDetails details, DependentStatus status, Integer age) {

    public Optional<Integer> ageInYears() {
        return Optional.ofNullable(age);
    }
}
