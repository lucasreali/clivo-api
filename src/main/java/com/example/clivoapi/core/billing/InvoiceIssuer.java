package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import org.springframework.stereotype.Component;

@Component
class InvoiceIssuer implements EncounterCompletionListener {

    private final BillingService billing;

    InvoiceIssuer(BillingService billing) {
        this.billing = billing;
    }

    @Override
    public void onCompleted(CompletedEncounter encounter) {
        billing.issueFor(encounter);
    }
}
