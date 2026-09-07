package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.customer.Address;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerSnapshot;
import com.example.clivoapi.core.customer.NationalId;
import com.example.clivoapi.core.customer.PostalCode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

record CustomerView(
        UUID id,
        String name,
        String nationalId,
        LocalDate birthDate,
        String phone,
        String email,
        String postalCode,
        String street,
        String status,
        String deactivationReason,
        boolean consented,
        Instant registeredAt) {

    static CustomerView of(CustomerSnapshot customer) {
        CustomerDetails details = customer.details();
        Optional<Address> residence = details.residence();
        return new CustomerView(
                customer.id(),
                details.name(),
                details.document().map(NationalId::asText).orElse(null),
                details.birthDate(),
                details.contact().phone().asText(),
                details.contact().reachableByEmail().map(EmailAddress::asText).orElse(null),
                residence.flatMap(Address::zone).map(PostalCode::asText).orElse(null),
                residence.flatMap(Address::line).orElse(null),
                customer.status().name(),
                customer.reasonForDeactivation().orElse(null),
                customer.consented(),
                customer.registeredAt());
    }
}
