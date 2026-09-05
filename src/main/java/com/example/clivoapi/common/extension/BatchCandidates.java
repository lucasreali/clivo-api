package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.Optional;

public record BatchCandidates(String productName, List<BatchCandidate> candidates) {

    public BatchCandidates {
        candidates = List.copyOf(candidates);
    }

    public Optional<BatchCandidate> firstFresh() {
        return candidates.stream().filter(BatchCandidate::isFresh).findFirst();
    }

    public Optional<BatchCandidate> first() {
        return candidates.stream().findFirst();
    }

    public boolean isEmpty() {
        return candidates.isEmpty();
    }
}
