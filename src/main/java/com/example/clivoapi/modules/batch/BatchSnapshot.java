package com.example.clivoapi.modules.batch;

import java.time.LocalDate;

public record BatchSnapshot(
        Long id, Long productId, String productName, BatchDetails details, BatchStatus status, boolean expired) {

    public LocalDate expiresOn() {
        return details.expiresOn();
    }
}
