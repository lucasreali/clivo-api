package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.Address;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerSnapshot;
import com.example.clivoapi.core.customer.NationalId;
import java.time.LocalDate;
import java.util.Optional;

record CustomerView(
        Long id,
        String name,
        String nationalId,
        LocalDate birthDate,
        String phone,
        String email,
        String postalCode,
        String street,
        String status,
        String deactivationReason,
        boolean consented) {

    static CustomerView of(CustomerSnapshot customer) {
        CustomerDetails details = customer.details();
        Optional<Address> residence = details.residence();
        return new CustomerView(
                customer.id(),
                details.name(),
                details.document().map(NationalId::asText).orElse(null),
                details.birthDate(),
                details.contact().phone(),
                details.contact().reachableByEmail().orElse(null),
                residence.map(Address::postalCode).orElse(null),
                residence.map(Address::street).orElse(null),
                customer.status().name(),
                customer.reasonForDeactivation().orElse(null),
                customer.consented());
    }
}
