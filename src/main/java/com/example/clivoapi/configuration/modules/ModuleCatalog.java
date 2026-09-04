package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ModuleCode;
import java.util.List;

public class ModuleCatalog {

    private final List<ModuleDefinition> definitions;

    public ModuleCatalog(List<ModuleDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    public ModuleDefinition definitionOf(ModuleCode module) {
        return definitions.stream()
                .filter(definition -> definition.hasCode(module))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Module", module));
    }

    public List<ModuleCode> dependentsOf(ModuleCode module) {
        return definitions.stream()
                .filter(definition -> definition.dependsOn(module))
                .map(ModuleDefinition::code)
                .toList();
    }

    public List<ModuleStatus> statusWithin(ActiveModules active) {
        return definitions.stream()
                .map(definition -> ModuleStatus.of(definition, active))
                .toList();
    }

    public List<ModuleDefinition> restrictedTo(ActiveModules active) {
        return definitions.stream()
                .filter(definition -> active.contains(definition.code()))
                .toList();
    }
}
