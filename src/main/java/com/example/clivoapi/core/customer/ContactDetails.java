package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.core.access.EmailAddress;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Optional;

@Embeddable
public record ContactDetails(@Embedded PhoneNumber phone, @Embedded EmailAddress email) {

    public ContactDetails {
        phone = requiredPhone(phone);
    }

    public Optional<EmailAddress> reachableByEmail() {
        return Optional.ofNullable(email);
    }

    private static PhoneNumber requiredPhone(PhoneNumber phone) {
        if (phone == null) {
            throw new BusinessException("phone: a phone number is required");
        }
        return phone;
    }
}
