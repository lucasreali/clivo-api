package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.MarkedRegionState;
import java.time.Instant;

public record MarkState(String mark, String note, Instant recordedAt) {

    static MarkState of(MarkedRegionState state) {
        if (state == null) {
            return null;
        }
        return new MarkState(state.mark(), state.note(), state.origin().recordedAt());
    }
}
