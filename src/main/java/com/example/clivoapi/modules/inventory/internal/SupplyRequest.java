package com.example.clivoapi.modules.inventory.internal;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record SupplyRequest(@NotNull Long productId, @NotNull BigDecimal quantity) {
}
