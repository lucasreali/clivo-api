package com.example.clivoapi.modules.inventory;

public record StockEntry(StockMovementType type, Quantity quantity, MovementReason reason) {

    public StockEntry {
        quantity.requirePositive("a movement quantity");
    }

    public void applyTo(Product product) {
        type.applyTo(product, quantity);
    }
}
