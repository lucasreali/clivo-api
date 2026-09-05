package com.example.clivoapi.common.extension;

import java.math.BigDecimal;

public record SuppliesUsed(Long encounterId, Long productId, BigDecimal quantity) {

    public SuppliesUsed {
        if (encounterId == null || productId == null) {
            throw new IllegalArgumentException("supplies belong to an encounter and to a product");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("a dispensed quantity is greater than zero");
        }
    }
}
