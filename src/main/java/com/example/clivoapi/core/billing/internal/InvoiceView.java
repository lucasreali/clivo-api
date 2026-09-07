package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.InvoiceAmounts;
import com.example.clivoapi.core.billing.InvoiceLine;
import com.example.clivoapi.core.billing.InvoiceSnapshot;
import com.example.clivoapi.core.billing.PaymentSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record InvoiceView(
        UUID id,
        UUID encounterId,
        UUID customerId,
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
        List<LineView> lines,
        List<PaymentView> payments) {

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
                invoice.lines().stream().map(LineView::of).toList(),
                invoice.payments().stream().map(PaymentView::of).toList());
    }

    private static BigDecimal amountOf(Money money) {
        return money.amount();
    }

    record LineView(UUID serviceId, String description, BigDecimal quantity, BigDecimal unitPrice) {

        static LineView of(InvoiceLine line) {
            return new LineView(line.serviceId(), line.description(), line.quantity(), line.unitPrice().amount());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record PaymentView(
            UUID id,
            BigDecimal amount,
            String method,
            Instant paidAt,
            Instant refundedAt,
            String refundReason,
            boolean refunded) {

        static PaymentView of(PaymentSnapshot payment) {
            return new PaymentView(
                    payment.id(),
                    payment.amount().amount(),
                    payment.method().name(),
                    payment.paidAt(),
                    payment.refundedAt(),
                    payment.refundReason(),
                    payment.isRefunded());
        }
    }
}
