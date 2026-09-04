package com.example.clivoapi.core.customer;

import java.time.LocalDate;
import java.util.Optional;

public record CustomerDetails(
        String name,
        NationalId nationalId,
        LocalDate birthDate,
        ContactDetails contact,
        Address address) {

    public Optional<NationalId> document() {
        return Optional.ofNullable(nationalId);
    }

    public Optional<Address> residence() {
        return Optional.ofNullable(address);
    }
}
