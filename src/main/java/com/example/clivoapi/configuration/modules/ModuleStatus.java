package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleCode;

public record ModuleStatus(ModuleCode code, String name, String description, ModuleCode dependency, boolean active) {

    static ModuleStatus of(ModuleDefinition definition, ActiveModules active) {
        return new ModuleStatus(
                definition.code(),
                definition.name(),
                definition.description(),
                definition.dependency().orElse(null),
                active.contains(definition.code()));
    }
}
