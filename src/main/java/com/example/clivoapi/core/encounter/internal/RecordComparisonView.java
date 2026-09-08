package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.FieldComparison;
import com.example.clivoapi.core.encounter.MarkState;
import com.example.clivoapi.core.encounter.MarkingChange;
import com.example.clivoapi.core.encounter.RecordComparison;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

record RecordComparisonView(Instant asOf, List<FieldComparisonView> fields) {

    static RecordComparisonView of(RecordComparison comparison) {
        return new RecordComparisonView(
                comparison.asOf(), comparison.fields().stream().map(FieldComparisonView::of).toList());
    }

    record FieldComparisonView(String fieldCode, List<MarkingChangeView> changes) {

        static FieldComparisonView of(FieldComparison field) {
            return new FieldComparisonView(
                    field.fieldCode(), field.changes().stream().map(MarkingChangeView::of).toList());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MarkingChangeView(String region, String part, String change, MarkView from, MarkView to) {

        static MarkingChangeView of(MarkingChange change) {
            return new MarkingChangeView(
                    change.region(),
                    change.part(),
                    change.change().name(),
                    MarkView.of(change.from()),
                    MarkView.of(change.to()));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MarkView(String mark, String note, Instant recordedAt) {

        static MarkView of(MarkState state) {
            if (state == null) {
                return null;
            }
            return new MarkView(state.mark(), state.note(), state.recordedAt());
        }
    }
}
