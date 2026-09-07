package com.example.clivoapi.modules.inventory.internal;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

record SupplyRequest(@NotNull UUID productId, @NotNull BigDecimal quantity) {
}
