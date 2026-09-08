package com.example.clivoapi.core.encounter;

import java.util.List;

public record FieldComparison(String fieldCode, List<MarkingChange> changes) {

    public FieldComparison {
        changes = List.copyOf(changes);
    }
}
