package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Quantity(@Column(name = "quantity", nullable = false, precision = 10, scale = 2) BigDecimal amount) {

    private static final int SCALE = 2;

    public Quantity {
        amount = nonNegative(amount);
    }

    public static Quantity of(String amount) {
        return new Quantity(new BigDecimal(amount));
    }

    public static Quantity none() {
        return new Quantity(BigDecimal.ZERO);
    }

    public Quantity plus(Quantity other) {
        return new Quantity(amount.add(other.amount));
    }

    public Quantity minus(Quantity other) {
        return new Quantity(amount.subtract(other.amount));
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isLessThan(Quantity other) {
        return amount.compareTo(other.amount) < 0;
    }

    public Quantity requirePositive(String subject) {
        if (isPositive()) {
            return this;
        }
        throw new BusinessException("%s is greater than zero".formatted(subject));
    }

    private static BigDecimal nonNegative(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("a quantity is required");
        }
        if (amount.signum() < 0) {
            throw new BusinessException("a quantity cannot be negative");
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
