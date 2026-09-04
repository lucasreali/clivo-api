package com.example.clivoapi.core.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(name = "postal_code", length = 8) String postalCode,
        @Column(name = "street", length = 160) String street) {
}
