package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.money.Money;

public record InvoiceAdjustment(String coverage, Money reduction, String reason) {

    public InvoiceAdjustment {
        if (coverage == null || coverage.isBlank()) {
            throw new IllegalArgumentException("an adjustment declares what covers the invoice");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("an adjustment declares why the amount was reduced");
        }
    }

    public boolean reducesAnything() {
        return !reduction.isZero();
    }
}
