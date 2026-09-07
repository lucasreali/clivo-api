package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.customer.Address;
import com.example.clivoapi.core.customer.ContactDetails;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.NationalId;
import com.example.clivoapi.core.customer.PhoneNumber;
import com.example.clivoapi.core.customer.PostalCode;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

record CustomerRequest(
        @NotBlank String name,
        String nationalId,
        LocalDate birthDate,
        @NotBlank String phone,
        String email,
        String postalCode,
        String street) {

    CustomerDetails toDetails() {
        return new CustomerDetails(name, document().orElse(null), birthDate, contact(), residence());
    }

    private Optional<NationalId> document() {
        return stated(nationalId, NationalId::new);
    }

    private ContactDetails contact() {
        return new ContactDetails(new PhoneNumber(phone), stated(email, EmailAddress::new).orElse(null));
    }

    private Address residence() {
        return new Address(stated(postalCode, PostalCode::new).orElse(null), street);
    }

    private static <T> Optional<T> stated(String value, Function<String, T> reading) {
        return Optional.ofNullable(value).filter(text -> !text.isBlank()).map(reading);
    }
}
