package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.MarkedRegionState;
import com.example.clivoapi.common.extension.MarkingOrigin;
import com.example.clivoapi.common.extension.MarkingSource;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.core.encounter.internal.EncounterRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
class MarkingProvenance {

    private final EncounterRepository encounters;

    MarkingProvenance(EncounterRepository encounters) {
        this.encounters = encounters;
    }

    RecordSheet resolve(Encounter current, RecordSheet sheet) {
        List<Encounter> lineage = lineageOf(current);
        return sheet.withFieldsResolved(field -> resolveField(field, lineage, current));
    }

    List<MarkedRegionState> stateAt(Encounter current, String fieldCode, Instant asOf) {
        return foldOver(baselineOf(current, asOf), fieldCode, current);
    }

    private SheetField resolveField(SheetField field, List<Encounter> lineage, Encounter current) {
        if (!field.describesRegions()) {
            return field;
        }
        return field.resolvedWith(foldOver(lineage, field.code(), current));
    }

    private List<MarkedRegionState> foldOver(List<Encounter> lineage, String fieldCode, Encounter current) {
        MarkingTimeline timeline = new MarkingTimeline();
        lineage.forEach(encounter -> record(timeline, encounter, fieldCode, current));
        return timeline.state();
    }

    private void record(MarkingTimeline timeline, Encounter encounter, String fieldCode, Encounter current) {
        encounter.filling()
                .values()
                .valueOf(fieldCode)
                .ifPresent(value -> timeline.record(value, originOf(encounter, current)));
    }

    private MarkingOrigin originOf(Encounter encounter, Encounter current) {
        Instant recordedAt = encounter.recordedAt();
        return new MarkingOrigin(
                sourceOf(encounter, current),
                recordedAt,
                recordedAt,
                encounter.id(),
                encounter.practitionerName());
    }

    private MarkingSource sourceOf(Encounter encounter, Encounter current) {
        if (encounter.id().equals(current.id())) {
            return MarkingSource.SESSION;
        }
        return MarkingSource.HISTORY;
    }

    private List<Encounter> lineageOf(Encounter current) {
        return chronologically(historyBefore(current), current);
    }

    private List<Encounter> baselineOf(Encounter current, Instant asOf) {
        return historyBefore(current).stream()
                .filter(encounter -> !encounter.recordedAt().isAfter(asOf))
                .sorted(Comparator.comparing(Encounter::recordedAt))
                .toList();
    }

    private List<Encounter> chronologically(List<Encounter> history, Encounter current) {
        return Stream.concat(
                        history.stream().sorted(Comparator.comparing(Encounter::recordedAt)),
                        Stream.of(current))
                .toList();
    }

    private List<Encounter> historyBefore(Encounter current) {
        return encounters.findByCustomerIdOrderByStartedAtDesc(current.customerId()).stream()
                .filter(Encounter::isCompleted)
                .filter(encounter -> !encounter.id().equals(current.id()))
                .filter(encounter -> encounter.recordedAt().isBefore(current.recordedAt()))
                .toList();
    }
}
