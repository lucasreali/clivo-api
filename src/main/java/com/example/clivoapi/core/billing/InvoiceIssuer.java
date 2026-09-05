package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(InvoiceIssuer.FIRST)
class InvoiceIssuer implements EncounterCompletionListener {

    static final int FIRST = 0;

    private final BillingService billing;

    InvoiceIssuer(BillingService billing) {
        this.billing = billing;
    }

    @Override
    public void onCompleted(CompletedEncounter encounter) {
        billing.issueFor(encounter);
    }
}
