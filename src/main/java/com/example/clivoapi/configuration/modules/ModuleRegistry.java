package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.internal.ModuleActivationRepository;
import com.example.clivoapi.configuration.modules.internal.ModuleDefinitionRepository;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class ModuleRegistry {

    private final ModuleDefinitionRepository definitions;
    private final ModuleActivationRepository activations;

    ModuleRegistry(ModuleDefinitionRepository definitions, ModuleActivationRepository activations) {
        this.definitions = definitions;
        this.activations = activations;
    }

    public ModuleCatalog catalog() {
        return new ModuleCatalog(definitions.findAll());
    }

    public ActiveModules activeModules() {
        return new ActiveModules(activations.findAll());
    }

    public ModuleDefinition definitionOf(ModuleCode module) {
        return catalog().definitionOf(module);
    }

    public List<ModuleCode> dependentsOf(ModuleCode module) {
        return catalog().dependentsOf(module);
    }
}
