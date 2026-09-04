package com.example.clivoapi.core.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Optional;

@Embeddable
public record ContactDetails(
        @Column(name = "phone", nullable = false, length = 20) String phone,
        @Column(name = "email", length = 160) String email) {

    public ContactDetails {
        phone = requiredPhone(phone);
    }

    public Optional<String> reachableByEmail() {
        return Optional.ofNullable(email).filter(address -> !address.isBlank());
    }

    private static String requiredPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("a phone number is required");
        }
        return phone.trim();
    }
}
