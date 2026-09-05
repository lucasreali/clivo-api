package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.MovementReason;
import com.example.clivoapi.modules.inventory.Quantity;
import com.example.clivoapi.modules.inventory.StockEntry;
import com.example.clivoapi.modules.inventory.StockMovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record StockEntryRequest(@NotBlank String type, @NotNull BigDecimal quantity, String reason) {

    StockEntry toEntry() {
        return new StockEntry(StockMovementType.of(type), new Quantity(quantity), new MovementReason(reason));
    }
}
