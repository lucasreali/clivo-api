package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

record ProductRequest(@NotBlank String name, @NotBlank String unit, BigDecimal minStock, boolean batchControlled) {

    ProductDetails toDetails() {
        return new ProductDetails(name, new MeasurementUnit(unit), minimum(), batchControlled);
    }

    private Quantity minimum() {
        return minStock == null ? Quantity.none() : new Quantity(minStock);
    }
}
