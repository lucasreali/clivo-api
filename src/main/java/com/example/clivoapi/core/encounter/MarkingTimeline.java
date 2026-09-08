package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.MarkedPart;
import com.example.clivoapi.common.extension.MarkedRegionState;
import com.example.clivoapi.common.extension.MarkingOrigin;
import com.example.clivoapi.common.extension.RegionMarking;
import com.example.clivoapi.common.extension.RegionMarkings;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MarkingTimeline {

    private final Map<MarkedPart, MarkedRegionState> resolved = new LinkedHashMap<>();

    void record(Object value, MarkingOrigin origin) {
        RegionMarkings.in(value)
                .ifPresent(markings -> markings.markings().forEach(marking -> apply(marking, origin)));
    }

    List<MarkedRegionState> state() {
        return List.copyOf(resolved.values());
    }

    private void apply(RegionMarking marking, MarkingOrigin origin) {
        MarkedPart.covering(marking).forEach(part -> put(part, marking, origin));
    }

    private void put(MarkedPart part, RegionMarking marking, MarkingOrigin origin) {
        MarkedRegionState state =
                new MarkedRegionState(part.region(), part.part(), marking.mark(), marking.note(), origin);
        resolved.put(part, state.continuing(resolved.get(part)));
    }
}
