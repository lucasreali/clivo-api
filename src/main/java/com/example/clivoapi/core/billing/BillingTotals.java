package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;

public record BillingTotals(Money gross, Money discount, Money net, Money outstanding) {

    public Money received() {
        return net.minus(outstanding);
    }
}
