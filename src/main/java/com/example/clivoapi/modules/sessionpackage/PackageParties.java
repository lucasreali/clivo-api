package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.customer.CustomerService;

class PackageParties {

    private final CustomerService customers;
    private final CatalogService catalogue;

    PackageParties(CustomerService customers, CatalogService catalogue) {
        this.customers = customers;
        this.catalogue = catalogue;
    }

    SessionPackage assemble(PackagePurchase purchase) {
        return new SessionPackage(
                customers.reference(purchase.customerId()), catalogue.reference(purchase.serviceId()), purchase);
    }
}
