package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record CoveragePercentage(
        @Column(name = "reimbursement_pct", nullable = false, precision = 5, scale = 2) BigDecimal value) {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public CoveragePercentage {
        value = withinRange(value);
    }

    public static CoveragePercentage of(String value) {
        return new CoveragePercentage(new BigDecimal(value));
    }

    public Money appliedTo(Money gross) {
        return new Money(gross.amount().multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_UP));
    }

    public boolean isNone() {
        return value.signum() == 0;
    }

    private static BigDecimal withinRange(BigDecimal value) {
        if (value == null) {
            throw new BusinessException("a plan declares how much of the bill it reimburses");
        }
        if (value.signum() < 0 || value.compareTo(HUNDRED) > 0) {
            throw new BusinessException("a reimbursement percentage lies between 0 and 100");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
