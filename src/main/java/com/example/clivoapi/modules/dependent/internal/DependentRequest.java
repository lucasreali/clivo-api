package com.example.clivoapi.modules.dependent.internal;

import com.example.clivoapi.modules.dependent.DependentAttributes;
import com.example.clivoapi.modules.dependent.DependentDetails;
import com.example.clivoapi.modules.dependent.DependentType;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;

record DependentRequest(
        @NotBlank String name, @NotBlank String type, LocalDate birthDate, Map<String, Object> attributes) {

    DependentDetails toDetails() {
        return new DependentDetails(name, DependentType.of(type), birthDate, new DependentAttributes(attributes));
    }
}
