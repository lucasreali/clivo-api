package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.PractitionerDetails;
import jakarta.validation.constraints.NotBlank;

record PractitionerRequest(@NotBlank String name, String licenseNumber) {

    PractitionerDetails toDetails() {
        return new PractitionerDetails(name, licenseNumber);
    }
}
