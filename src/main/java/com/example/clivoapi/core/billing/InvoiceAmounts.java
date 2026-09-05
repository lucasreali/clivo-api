package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;

public record InvoiceAmounts(Money gross, Money discount, Money net, Money outstanding) {
}
