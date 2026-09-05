package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.DiscountReason;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record DiscountRequest(@NotNull BigDecimal amount, String reason) {

    Money toAmount() {
        return new Money(amount);
    }

    DiscountReason toReason() {
        return new DiscountReason(reason);
    }
}
