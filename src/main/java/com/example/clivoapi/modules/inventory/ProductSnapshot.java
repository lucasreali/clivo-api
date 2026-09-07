package com.example.clivoapi.modules.inventory;

import java.util.UUID;

public record ProductSnapshot(
        UUID id, ProductDetails details, Quantity onHand, ProductStatus status, boolean belowMinimum) {
}
