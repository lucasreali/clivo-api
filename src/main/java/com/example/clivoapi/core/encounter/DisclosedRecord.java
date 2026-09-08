package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.RecordAssembly;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.SheetField;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
class DisclosedRecord {

    private final RecordAssembly records;
    private final MarkingProvenance provenance;

    DisclosedRecord(RecordAssembly records, MarkingProvenance provenance) {
        this.records = records;
        this.provenance = provenance;
    }

    RecordSheet of(Encounter encounter) {
        return provenance.resolve(encounter, plainOf(encounter));
    }

    RecordSheet plainOf(Encounter encounter) {
        return records.assemble(encounter.filling());
    }

    RecordComparison compare(Encounter encounter, Instant asOf) {
        return new RecordComparison(
                asOf,
                of(encounter).regionFields().stream()
                        .map(field -> compareField(encounter, field, asOf))
                        .toList());
    }

    private FieldComparison compareField(Encounter encounter, SheetField field, Instant asOf) {
        return new FieldComparison(
                field.code(),
                MarkingDiff.between(provenance.stateAt(encounter, field.code(), asOf), field.markings()));
    }
}
