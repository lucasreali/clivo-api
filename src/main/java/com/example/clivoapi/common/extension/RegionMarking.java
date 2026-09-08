package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.Optional;

public record RegionMarking(String region, List<String> parts, String mark, String note) {

    public RegionMarking {
        parts = List.copyOf(Optional.ofNullable(parts).orElseGet(List::of));
    }

    public boolean coversTheWholeRegion() {
        return parts.isEmpty();
    }

    public List<String> markedParts() {
        return parts;
    }

    public Optional<String> writtenNote() {
        return Optional.ofNullable(note);
    }
}
