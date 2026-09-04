package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleStatus;

record ModuleView(String code, String name, String description, String requiresModule, boolean active) {

    static ModuleView of(ModuleStatus status) {
        return new ModuleView(
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
