package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.exception.BusinessException;
import java.time.LocalDate;
import java.util.Optional;

public record DependentDetails(
        String name, DependentType type, LocalDate birthDate, DependentAttributes attributes) {

    public DependentDetails {
        name = named(name);
        type = typed(type);
        attributes = Optional.ofNullable(attributes).orElseGet(DependentAttributes::none);
    }

    public Optional<LocalDate> dateOfBirth() {
        return Optional.ofNullable(birthDate);
    }

    private static String named(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("a dependent needs a name");
        }
        return name.trim();
    }

    private static DependentType typed(DependentType type) {
        if (type == null) {
            throw new BusinessException("a dependent needs a type");
        }
        return type;
    }
}
