package com.example.clivoapi.core.billing;

public enum InvoiceStatus {

    OPEN,
    PAID,
    PARTIAL,
    CANCELLED;

    public boolean isSettled() {
        return this == PAID;
    }

    public boolean acceptsChange() {
        return this == OPEN || this == PARTIAL;
    }
}
