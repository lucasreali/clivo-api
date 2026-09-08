package com.example.clivoapi.core.encounter;

import java.time.Instant;
import java.util.List;

public record RecordComparison(Instant asOf, List<FieldComparison> fields) {

    public RecordComparison {
        fields = List.copyOf(fields);
    }
}
