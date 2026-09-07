package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Optional;

@Embeddable
public record Address(
        @Embedded PostalCode postalCode,
        @Column(name = "street", length = 160) String street) {

    private static final int MAXIMUM_STREET_LENGTH = 160;

    public Address {
        street = trimmedStreet(street);
    }

    public Optional<PostalCode> zone() {
        return Optional.ofNullable(postalCode);
    }

    public Optional<String> line() {
        return Optional.ofNullable(street);
    }

    private static String trimmedStreet(String street) {
        if (street == null || street.isBlank()) {
            return null;
        }
        if (street.trim().length() > MAXIMUM_STREET_LENGTH) {
            throw new BusinessException(
                    "street: limited to %d characters".formatted(MAXIMUM_STREET_LENGTH));
        }
        return street.trim();
    }
}
