package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;

public record PaymentDetails(Money amount, PaymentMethod method) {

    public PaymentDetails {
        requirePositive(amount);
        requireMethod(method);
    }

    private static void requirePositive(Money amount) {
        if (amount != null && !amount.isZero()) {
            return;
        }
        throw new BusinessException("a payment needs an amount above zero");
    }

    private static void requireMethod(PaymentMethod method) {
        if (method != null) {
            return;
        }
        throw new BusinessException("a payment needs a method");
    }
}
