package com.example.clivoapi.modules.dependent.internal;

import com.example.clivoapi.modules.dependent.DependentDetails;
import com.example.clivoapi.modules.dependent.DependentSnapshot;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

record DependentView(
        UUID id,
        UUID customerId,
        String name,
        String type,
        String custodian,
        LocalDate birthDate,
        Integer ageInYears,
        Map<String, Object> attributes,
        String status) {

    static DependentView of(DependentSnapshot dependent) {
        DependentDetails details = dependent.details();
        return new DependentView(
                dependent.id(),
                dependent.customerId(),
                details.name(),
                details.type().name(),
                details.type().custodianTitle(),
                details.dateOfBirth().orElse(null),
                dependent.ageInYears().orElse(null),
                details.attributes().asMap(),
                dependent.status().name());
    }
}
