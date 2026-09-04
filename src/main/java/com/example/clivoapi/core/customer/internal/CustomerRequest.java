package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.Address;
import com.example.clivoapi.core.customer.ContactDetails;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.NationalId;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Optional;

record CustomerRequest(
        @NotBlank String name,
        String nationalId,
        LocalDate birthDate,
        @NotBlank String phone,
        String email,
        String postalCode,
        String street) {

    CustomerDetails toDetails() {
        return new CustomerDetails(name, document(), birthDate, new ContactDetails(phone, email), residence());
    }

    private NationalId document() {
        return Optional.ofNullable(nationalId).filter(value -> !value.isBlank()).map(NationalId::new).orElse(null);
    }

    private Address residence() {
        return new Address(postalCode, street);
    }
}
