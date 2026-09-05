package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record CommissionRate(
        @Column(name = "percentage", nullable = false, precision = 5, scale = 2) BigDecimal value) {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public CommissionRate {
        value = withinRange(value);
    }

    public static CommissionRate of(String value) {
        return new CommissionRate(new BigDecimal(value));
    }

    public Money appliedTo(Money amount) {
        return new Money(amount.amount().multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_UP));
    }

    public boolean isNone() {
        return value.signum() == 0;
    }

    private static BigDecimal withinRange(BigDecimal value) {
        if (value == null) {
            throw new BusinessException("a practitioner earns a declared share of what is billed");
        }
        if (value.signum() < 0 || value.compareTo(HUNDRED) > 0) {
            throw new BusinessException("a commission rate lies between 0 and 100");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
