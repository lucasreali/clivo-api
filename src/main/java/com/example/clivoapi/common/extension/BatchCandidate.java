package com.example.clivoapi.common.extension;

import java.time.LocalDate;
import java.util.UUID;

public record BatchCandidate(UUID batchId, String code, LocalDate expiresOn, boolean expired) {

    public BatchCandidate {
        if (batchId == null || code == null || expiresOn == null) {
            throw new IllegalArgumentException("a batch candidate is identified by id, code and expiry date");
        }
    }

    public boolean isFresh() {
        return !expired;
    }
}
