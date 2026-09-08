package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.MarkedPart;
import com.example.clivoapi.common.extension.MarkedRegionState;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class MarkingDiff {

    private final Map<MarkedPart, MarkedRegionState> before;
    private final Map<MarkedPart, MarkedRegionState> after;

    private MarkingDiff(List<MarkedRegionState> before, List<MarkedRegionState> after) {
        this.before = byPart(before);
        this.after = byPart(after);
    }

    static List<MarkingChange> between(List<MarkedRegionState> before, List<MarkedRegionState> after) {
        return new MarkingDiff(before, after).changes();
    }

    private List<MarkingChange> changes() {
        Set<MarkedPart> touched = new LinkedHashSet<>(before.keySet());
        touched.addAll(after.keySet());
        return touched.stream().map(this::changeAt).flatMap(Optional::stream).toList();
    }

    private Optional<MarkingChange> changeAt(MarkedPart part) {
        MarkedRegionState was = before.get(part);
        MarkedRegionState is = after.get(part);
        return kindOf(was, is)
                .map(kind -> new MarkingChange(
                        part.region(), part.part(), kind, MarkState.of(was), MarkState.of(is)));
    }

    private Optional<ChangeKind> kindOf(MarkedRegionState was, MarkedRegionState is) {
        if (was == null) {
            return Optional.of(ChangeKind.ADDED);
        }
        if (is == null) {
            return Optional.of(ChangeKind.REMOVED);
        }
        if (Objects.equals(was.mark(), is.mark())) {
            return Optional.empty();
        }
        return Optional.of(ChangeKind.CHANGED);
    }

    private static Map<MarkedPart, MarkedRegionState> byPart(List<MarkedRegionState> states) {
        Map<MarkedPart, MarkedRegionState> indexed = new LinkedHashMap<>();
        states.forEach(state -> indexed.put(new MarkedPart(state.region(), state.part()), state));
        return indexed;
    }
}
