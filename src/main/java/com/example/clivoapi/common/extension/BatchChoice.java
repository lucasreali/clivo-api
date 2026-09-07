package com.example.clivoapi.common.extension;

import java.util.Optional;
import java.util.UUID;

public record BatchChoice(UUID batchId, String code, String warning) {

    public static BatchChoice accepted(BatchCandidate candidate) {
        return new BatchChoice(candidate.batchId(), candidate.code(), null);
    }

    public static BatchChoice warned(BatchCandidate candidate, String warning) {
        return new BatchChoice(candidate.batchId(), candidate.code(), warning);
    }

    public Optional<String> alert() {
        return Optional.ofNullable(warning);
    }
}
