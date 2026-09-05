package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.money.Money;
import java.util.List;

public record CommissionStatement(CommissionPeriod period, List<CommissionSnapshot> commissions, Money total) {

    public static CommissionStatement of(CommissionPeriod period, List<CommissionSnapshot> commissions) {
        return new CommissionStatement(period, List.copyOf(commissions), totalOf(commissions));
    }

    public boolean isClosed() {
        return !commissions.isEmpty() && commissions.stream().noneMatch(commission -> commission.status().isOpen());
    }

    private static Money totalOf(List<CommissionSnapshot> commissions) {
        return commissions.stream().map(CommissionSnapshot::amount).reduce(Money.zero(), Money::plus);
    }
}
