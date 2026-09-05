package com.example.clivoapi.core.encounter;

import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.practitioner.PractitionerService;

class EncounterParties {

    private final CustomerService customers;
    private final PractitionerService practitioners;

    EncounterParties(CustomerService customers, PractitionerService practitioners) {
        this.customers = customers;
        this.practitioners = practitioners;
    }

    Customer customer(Long id) {
        return customers.reference(id);
    }

    Practitioner practitioner(Long id) {
        return practitioners.reference(id);
    }
}
