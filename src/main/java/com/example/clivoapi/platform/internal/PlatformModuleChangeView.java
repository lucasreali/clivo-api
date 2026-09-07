package com.example.clivoapi.platform.internal;

import com.example.clivoapi.configuration.modules.ModuleActivationRecord;
import java.time.Instant;
import java.util.UUID;

record PlatformModuleChangeView(String code, String action, Instant changedAt, UUID author) {

    static PlatformModuleChangeView of(ModuleActivationRecord change) {
        return new PlatformModuleChangeView(
                change.module().value(), change.action().name(), change.changedAt(), change.author());
    }
}
