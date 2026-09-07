package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.Optional;
import java.util.UUID;

public record ScheduleBlockDetails(UUID practitionerId, TimeWindow period, BlockReason reason) {

    public Optional<UUID> practitioner() {
        return Optional.ofNullable(practitionerId);
    }
}
