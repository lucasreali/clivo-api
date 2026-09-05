package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;
import java.util.List;
import java.util.function.Function;

public record BillingReport(ReportPeriod period, int invoices, BillingTotals totals) {

    public static BillingReport of(ReportPeriod period, List<InvoiceSnapshot> issued) {
        return new BillingReport(period, issued.size(), totalsOf(issued));
    }

    private static BillingTotals totalsOf(List<InvoiceSnapshot> issued) {
        return new BillingTotals(
                sumOf(issued, amounts -> amounts.gross()),
                sumOf(issued, amounts -> amounts.discount()),
                sumOf(issued, amounts -> amounts.net()),
                sumOf(issued, amounts -> amounts.outstanding()));
    }

    private static Money sumOf(List<InvoiceSnapshot> issued, Function<InvoiceAmounts, Money> part) {
        return issued.stream()
                .map(InvoiceSnapshot::amounts)
                .map(part)
                .reduce(Money.zero(), Money::plus);
    }
}
