package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;
import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceLine(UUID serviceId, String description, BigDecimal quantity, Money unitPrice) {

    public Money total() {
        return new Money(unitPrice.amount().multiply(quantity));
    }
}
