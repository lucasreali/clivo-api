package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.text.TextField;
import java.util.Optional;

public record PractitionerDetails(String name, String licenseNumber) {

    private static final TextField NAME = new TextField("a practitioner name", 120);
    private static final TextField LICENSE = new TextField("a licence number", 30);

    public PractitionerDetails {
        name = NAME.required(name);
        licenseNumber = LICENSE.optional(licenseNumber);
    }

    public Optional<String> license() {
        return Optional.ofNullable(licenseNumber);
    }
}
