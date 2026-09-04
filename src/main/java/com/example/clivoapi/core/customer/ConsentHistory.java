package com.example.clivoapi.core.customer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ConsentHistory {

    private static final Comparator<ConsentRecord> BY_RECORDING = Comparator
            .comparing(ConsentRecord::recordedAt)
            .thenComparing(ConsentRecord::id, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final List<ConsentRecord> records;

    ConsentHistory(List<ConsentRecord> records) {
        this.records = List.copyOf(records);
    }

    public boolean grants(ConsentPurpose purpose) {
        return latestFor(purpose).map(ConsentRecord::isGranted).orElse(false);
    }

    private Optional<ConsentRecord> latestFor(ConsentPurpose purpose) {
        return records.stream()
                .filter(record -> record.isFor(purpose))
                .max(BY_RECORDING);
    }
}
