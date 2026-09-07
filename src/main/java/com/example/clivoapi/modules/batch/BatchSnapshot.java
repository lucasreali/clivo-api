package com.example.clivoapi.modules.batch;

import java.time.LocalDate;
import java.util.UUID;

public record BatchSnapshot(
        UUID id, UUID productId, String productName, BatchDetails details, BatchStatus status, boolean expired) {

    public LocalDate expiresOn() {
        return details.expiresOn();
    }
}
