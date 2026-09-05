package com.example.clivoapi.common.money;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Money(@Column(name = "amount", nullable = false, precision = 10, scale = 2) BigDecimal amount) {

    private static final int SCALE = 2;

    public Money {
        amount = nonNegative(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    private static BigDecimal nonNegative(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("an amount is required");
        }
        if (amount.signum() < 0) {
            throw new BusinessException("an amount cannot be negative");
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
