package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.BlockReason;
import com.example.clivoapi.core.scheduling.ScheduleBlockDetails;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

record ScheduleBlockRequest(
        UUID practitionerId,
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end,
        String reason) {

    ScheduleBlockDetails toDetails() {
        return new ScheduleBlockDetails(practitionerId, TimeWindow.of(start, end), new BlockReason(reason));
    }
}
