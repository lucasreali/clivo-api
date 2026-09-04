package com.example.clivoapi.core.practitioner;

import java.util.Optional;

public record PractitionerDetails(String name, String licenseNumber) {

    public Optional<String> license() {
        return Optional.ofNullable(licenseNumber).filter(number -> !number.isBlank());
    }
}
