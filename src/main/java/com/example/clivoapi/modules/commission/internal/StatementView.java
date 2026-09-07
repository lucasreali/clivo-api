package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.CommissionSnapshot;
import com.example.clivoapi.modules.commission.CommissionStatement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record StatementView(String period, BigDecimal total, boolean closed, List<CommissionView> commissions) {

    static StatementView of(CommissionStatement statement) {
        return new StatementView(
                statement.period().toString(),
                statement.total().amount(),
                statement.isClosed(),
                statement.commissions().stream().map(CommissionView::of).toList());
    }

    record CommissionView(
            UUID id,
            UUID encounterId,
            UUID practitionerId,
            String practitionerName,
            BigDecimal percentage,
            BigDecimal amount,
            String status) {

        static CommissionView of(CommissionSnapshot commission) {
            return new CommissionView(
                    commission.id(),
                    commission.encounterId(),
                    commission.practitionerId(),
                    commission.practitionerName(),
                    commission.percentage().value(),
                    commission.amount().amount(),
                    commission.status().name());
        }
    }
}
