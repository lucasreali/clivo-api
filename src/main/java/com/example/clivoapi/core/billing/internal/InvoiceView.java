package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.InvoiceAmounts;
import com.example.clivoapi.core.billing.InvoiceLine;
import com.example.clivoapi.core.billing.InvoiceSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
record InvoiceView(
        Long id,
        Long encounterId,
        Long customerId,
        String customerName,
        BigDecimal grossAmount,
        BigDecimal discount,
        String discountReason,
        BigDecimal netAmount,
        BigDecimal outstandingBalance,
        LocalDate dueDate,
        String status,
        String coverage,
        boolean overdue,
        List<LineView> lines) {

    static InvoiceView of(InvoiceSnapshot invoice) {
        InvoiceAmounts amounts = invoice.amounts();
        return new InvoiceView(
                invoice.id(),
                invoice.encounterId(),
                invoice.customerId(),
                invoice.customerName(),
                amountOf(amounts.gross()),
                amountOf(amounts.discount()),
                invoice.discountReason(),
                amountOf(amounts.net()),
                amountOf(amounts.outstanding()),
                invoice.dueDate(),
                invoice.status().name(),
                invoice.coverage().name(),
                invoice.overdue(),
                invoice.lines().stream().map(LineView::of).toList());
    }

    private static BigDecimal amountOf(Money money) {
        return money.amount();
    }

    record LineView(Long serviceId, String description, BigDecimal quantity, BigDecimal unitPrice) {

        static LineView of(InvoiceLine line) {
            return new LineView(line.serviceId(), line.description(), line.quantity(), line.unitPrice().amount());
        }
    }
}
