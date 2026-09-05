package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.billing.BillingReport;
import com.example.clivoapi.core.billing.BillingTotals;
import java.math.BigDecimal;
import java.time.LocalDate;

record BillingReportView(
        LocalDate from,
        LocalDate to,
        int invoices,
        BigDecimal gross,
        BigDecimal discount,
        BigDecimal net,
        BigDecimal received,
        BigDecimal outstanding) {

    static BillingReportView of(BillingReport report) {
        BillingTotals totals = report.totals();
        return new BillingReportView(
                report.period().from(),
                report.period().to(),
                report.invoices(),
                totals.gross().amount(),
                totals.discount().amount(),
                totals.net().amount(),
                totals.received().amount(),
                totals.outstanding().amount());
    }
}
