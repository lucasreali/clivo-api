package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.PaymentDetails;
import com.example.clivoapi.core.billing.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record PaymentRequest(
@NotNull BigDecimal amount, @NotNull PaymentMethod method) {

    PaymentDetails toDetails() {
        return new PaymentDetails(new Money(amount), method);
    }
}
