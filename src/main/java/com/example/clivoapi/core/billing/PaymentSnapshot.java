package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.money.Money;
import java.time.Instant;
import java.util.Optional;

public record PaymentSnapshot(
        Long id, Money amount, PaymentMethod method, Instant paidAt, Instant refundedAt, String refundReason) {

    public boolean isRefunded() {
        return refundedAt != null;
    }

    public Optional<String> reasonGiven() {
        return Optional.ofNullable(refundReason);
    }
}
