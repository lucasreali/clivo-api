package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.text.TextField;
import java.time.LocalDate;
import java.util.Optional;

public record CustomerDetails(
        String name,
        NationalId nationalId,
        LocalDate birthDate,
        ContactDetails contact,
        Address address) {

    private static final TextField NAME = new TextField("a customer name", 120);

    public CustomerDetails {
        name = NAME.required(name);
        birthDate = alreadyReached(birthDate);
        contact = requiredContact(contact);
    }

    public Optional<NationalId> document() {
        return Optional.ofNullable(nationalId);
    }

    public Optional<Address> residence() {
        return Optional.ofNullable(address);
    }

    public Optional<LocalDate> dateOfBirth() {
        return Optional.ofNullable(birthDate);
    }

    private static LocalDate alreadyReached(LocalDate birthDate) {
        if (birthDate == null || !birthDate.isAfter(LocalDate.now())) {
            return birthDate;
        }
        throw new BusinessException("birthDate cannot be in the future");
    }

    private static ContactDetails requiredContact(ContactDetails contact) {
        if (contact != null) {
            return contact;
        }
        throw new BusinessException("a customer is registered with a way to be reached");
    }
}
