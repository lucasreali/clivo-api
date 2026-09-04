package com.example.clivoapi.core.access;

import java.util.Optional;

public record SignInAttempt(String clinicCode, EmailAddress email, RawPassword password) {

    public Optional<String> clinic() {
        return Optional.ofNullable(clinicCode).filter(code -> !code.isBlank());
    }
}
