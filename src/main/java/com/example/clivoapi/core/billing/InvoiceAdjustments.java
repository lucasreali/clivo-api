package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.InvoiceAdjuster;
import com.example.clivoapi.common.extension.InvoiceAdjustment;
import java.util.List;
import java.util.Optional;

class InvoiceAdjustments {

    private final List<InvoiceAdjuster> adjusters;

    InvoiceAdjustments(List<InvoiceAdjuster> adjusters) {
        this.adjusters = List.copyOf(adjusters);
    }

    void applyTo(Invoice invoice, CompletedEncounter completed) {
        adjusters.stream()
                .map(adjuster -> adjuster.adjustmentFor(invoice.billableAs(completed)))
                .flatMap(Optional::stream)
                .filter(InvoiceAdjustment::reducesAnything)
                .forEach(invoice::adjustBy);
    }
}
