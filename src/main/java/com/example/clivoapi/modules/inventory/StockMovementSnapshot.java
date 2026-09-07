package com.example.clivoapi.modules.inventory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record StockMovementSnapshot(
        UUID id,
        UUID productId,
        StockMovementType type,
        Quantity quantity,
        String reason,
        UUID encounterId,
        UUID recordedBy,
        Instant recordedAt) {

    public Optional<String> statedReason() {
        return Optional.ofNullable(reason);
    }

    public Optional<UUID> encounter() {
        return Optional.ofNullable(encounterId);
    }
}
