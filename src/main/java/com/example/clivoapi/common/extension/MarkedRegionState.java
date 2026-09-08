package com.example.clivoapi.common.extension;

import java.util.Objects;

public record MarkedRegionState(String region, String part, String mark, String note, MarkingOrigin origin) {

    public MarkedRegionState continuing(MarkedRegionState earlier) {
        if (earlier == null || !Objects.equals(earlier.mark(), mark)) {
            return this;
        }
        return new MarkedRegionState(region, part, mark, note, origin.carriedFrom(earlier.origin()));
    }
}
