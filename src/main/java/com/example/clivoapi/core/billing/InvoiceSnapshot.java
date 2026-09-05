package com.example.clivoapi.core.billing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record InvoiceSnapshot(
        Long id,
        Long encounterId,
        Long customerId,
        String customerName,
        InvoiceAmounts amounts,
        String discountReason,
        LocalDate dueDate,
        InvoiceStatus status,
        InvoiceCoverage coverage,
        boolean overdue,
        List<InvoiceLine> lines) {

    public InvoiceSnapshot {
        lines = List.copyOf(lines);
    }

    public Optional<String> reasonGiven() {
        return Optional.ofNullable(discountReason);
    }
}
