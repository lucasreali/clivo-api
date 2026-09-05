package com.example.clivoapi.modules.inventory;

import java.time.Instant;
import java.util.Optional;

public record StockMovementSnapshot(
        Long id,
        Long productId,
        StockMovementType type,
        Quantity quantity,
        String reason,
        Long encounterId,
        Long recordedBy,
        Instant recordedAt) {

    public Optional<String> statedReason() {
        return Optional.ofNullable(reason);
    }

    public Optional<Long> encounter() {
        return Optional.ofNullable(encounterId);
    }
}
