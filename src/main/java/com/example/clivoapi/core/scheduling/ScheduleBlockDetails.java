package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.Optional;

public record ScheduleBlockDetails(Long practitionerId, TimeWindow period, BlockReason reason) {

    public Optional<Long> practitioner() {
        return Optional.ofNullable(practitionerId);
    }
}
