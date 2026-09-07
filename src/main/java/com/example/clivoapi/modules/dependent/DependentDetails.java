package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.text.TextField;
import java.time.LocalDate;
import java.util.Optional;

public record DependentDetails(
        String name, DependentType type, LocalDate birthDate, DependentAttributes attributes) {

    private static final TextField NAME = new TextField("a dependent name", 120);

    public DependentDetails {
        name = NAME.required(name);
        type = typed(type);
        birthDate = alreadyReached(birthDate);
        attributes = Optional.ofNullable(attributes).orElseGet(DependentAttributes::none);
    }

    public Optional<LocalDate> dateOfBirth() {
        return Optional.ofNullable(birthDate);
    }

    private static DependentType typed(DependentType type) {
        if (type == null) {
            throw new BusinessException("a dependent needs a type");
        }
        return type;
    }

    private static LocalDate alreadyReached(LocalDate birthDate) {
        if (birthDate == null || !birthDate.isAfter(LocalDate.now())) {
            return birthDate;
        }
        throw new BusinessException("birthDate cannot be in the future");
    }
}
