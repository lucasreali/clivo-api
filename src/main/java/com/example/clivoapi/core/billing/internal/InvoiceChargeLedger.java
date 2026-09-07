package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.extension.ChargeLedger;
import com.example.clivoapi.common.extension.EncounterCharges;
import com.example.clivoapi.core.billing.BillingService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class InvoiceChargeLedger implements ChargeLedger {

    private final BillingService billing;

    InvoiceChargeLedger(BillingService billing) {
        this.billing = billing;
    }

    @Override
    public EncounterCharges chargesOf(List<UUID> encounterIds) {
        return billing.chargesOf(encounterIds);
    }
}
