package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.modules.inventory.StockMovementSnapshot;
import java.math.BigDecimal;
import java.time.Instant;

record StockMovementView(
        Long id,
        Long productId,
        String type,
        BigDecimal quantity,
        String reason,
        Long encounterId,
        Long recordedBy,
        Instant recordedAt) {

    static StockMovementView of(StockMovementSnapshot movement) {
        return new StockMovementView(
                movement.id(),
                movement.productId(),
                movement.type().name(),
                movement.quantity().amount(),
                movement.statedReason().orElse(null),
                movement.encounter().orElse(null),
                movement.recordedBy(),
                movement.recordedAt());
    }
}
