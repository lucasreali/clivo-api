package com.example.clivoapi.core.scheduling.internal;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

record RescheduleRequest(@NotNull LocalDateTime start) {
}
