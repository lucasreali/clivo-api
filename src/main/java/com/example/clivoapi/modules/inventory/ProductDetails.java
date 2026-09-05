package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;

public record ProductDetails(String name, MeasurementUnit unit, Quantity minimum, boolean batchControlled) {

    public ProductDetails {
        name = named(name);
    }

    private static String named(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("a product needs a name");
        }
        return name.trim();
    }
}
