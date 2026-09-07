package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.ProductSnapshot;
import java.math.BigDecimal;
import java.util.UUID;

record ProductView(
        UUID id,
        String name,
        String unit,
        BigDecimal minStock,
        BigDecimal onHand,
        boolean batchControlled,
        boolean belowMinimum,
        String status) {

    static ProductView of(ProductSnapshot product) {
        ProductDetails details = product.details();
        return new ProductView(
                product.id(),
                details.name(),
                details.unit().asText(),
                details.minimum().amount(),
                product.onHand().amount(),
                details.batchControlled(),
                product.belowMinimum(),
                product.status().name());
    }
}
