package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Arrays;

public enum DependentType {

    ANIMAL("tutor"),
    MINOR("guardian"),
    ASSISTED("caregiver");

    private final String custodianTitle;

    DependentType(String custodianTitle) {
        this.custodianTitle = custodianTitle;
    }

    public static DependentType of(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "a dependent is one of %s".formatted(Arrays.toString(values()))));
    }

    public String custodianTitle() {
        return custodianTitle;
    }
}
