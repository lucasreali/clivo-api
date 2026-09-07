package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleStatus;

record PlatformModuleView(String code, String name, String description, String requiresModule, boolean active) {

    static PlatformModuleView of(ModuleStatus status) {
        return new PlatformModuleView(
                status.code().value(),
                status.name(),
                status.description(),
                textOf(status.dependency()),
                status.active());
    }

    private static String textOf(ModuleCode dependency) {
        return dependency == null ? null : dependency.value();
    }
}
