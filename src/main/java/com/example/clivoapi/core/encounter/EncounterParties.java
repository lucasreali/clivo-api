package com.example.clivoapi.core.encounter;

import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.practitioner.PractitionerService;
import java.util.UUID;

class EncounterParties {

    private final CustomerService customers;
    private final PractitionerService practitioners;
    private final CatalogService catalogue;

    EncounterParties(CustomerService customers, PractitionerService practitioners, CatalogService catalogue) {
        this.customers = customers;
        this.practitioners = practitioners;
        this.catalogue = catalogue;
    }

    Customer customer(UUID id) {
        return customers.reference(id);
    }

    Practitioner practitioner(UUID id) {
        return practitioners.reference(id);
    }

    Service service(UUID id) {
        return catalogue.reference(id);
    }
}
