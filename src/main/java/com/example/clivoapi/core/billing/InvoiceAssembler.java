package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.encounter.EncounterBook;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class InvoiceAssembler {

    private final EncounterBook encounters;
    private final BillingParties parties;

    InvoiceAssembler(EncounterBook encounters, CustomerService customers, CatalogService catalogue) {
        this.encounters = encounters;
        this.parties = new BillingParties(customers, catalogue);
    }

    public Invoice assemble(CompletedEncounter completed) {
        return new Invoice(
                encounters.reference(completed.encounterId()),
                parties.customer(completed.customerId()),
                parties.service(completed.serviceId()),
                LocalDate.now());
    }
}
