package com.example.clivoapi.core.billing;

import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import com.example.clivoapi.core.customer.CustomerService;

class BillingParties {

    private final CustomerService customers;
    private final CatalogService catalogue;

    BillingParties(CustomerService customers, CatalogService catalogue) {
        this.customers = customers;
        this.catalogue = catalogue;
    }

    Customer customer(Long id) {
        return customers.reference(id);
    }

    Service service(Long id) {
        return catalogue.reference(id);
    }
}
