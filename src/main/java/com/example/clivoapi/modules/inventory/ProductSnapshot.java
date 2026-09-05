package com.example.clivoapi.modules.inventory;

public record ProductSnapshot(
        Long id, ProductDetails details, Quantity onHand, ProductStatus status, boolean belowMinimum) {
}
