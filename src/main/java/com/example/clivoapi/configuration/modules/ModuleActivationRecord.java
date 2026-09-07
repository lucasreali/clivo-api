package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ActivationIntent;
import com.example.clivoapi.common.extension.ModuleCode;
import java.time.Instant;
import java.util.UUID;

public record ModuleActivationRecord(ModuleCode module, ActivationIntent action, Instant changedAt, UUID author) {
}
