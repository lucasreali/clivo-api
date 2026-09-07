package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleActivationProposal;
import com.example.clivoapi.common.extension.ModuleActivationValidator;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.internal.ModuleActivationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleActivationService {

    private final ModuleRegistry registry;
    private final ModuleActivationRepository activations;
    private final ModuleActivationValidation validation;
    private final ModuleActivationTrail trail;

    ModuleActivationService(
            ModuleRegistry registry,
            ModuleActivationRepository activations,
            List<ModuleActivationValidator> validators,
            ModuleActivationTrail trail) {
        this.registry = registry;
        this.activations = activations;
        this.validation = new ModuleActivationValidation(validators);
        this.trail = trail;
    }

    public void activate(ModuleCode module) {
        ModuleDefinition definition = registry.definitionOf(module);
        validation.check(ModuleActivationProposal.toActivate(module));
        ModuleActivation activation = activationOf(definition);
        trail.record(activation.enable(trail.author()));
        activations.save(activation);
    }

    public void deactivate(ModuleCode module) {
        ModuleDefinition definition = registry.definitionOf(module);
        validation.check(ModuleActivationProposal.toDeactivate(module));
        ModuleActivation activation = activationOf(definition);
        trail.record(activation.disable(trail.author()));
        activations.save(activation);
    }

    @Transactional(readOnly = true)
    public List<ModuleStatus> statusOfAll() {
        return registry.catalog().statusWithin(registry.activeModules());
    }

    @Transactional(readOnly = true)
    public List<ModuleActivationRecord> history() {
        return trail.entries();
    }

    @Transactional(readOnly = true)
    public List<ModuleDefinition> activeDefinitions() {
        return registry.catalog().restrictedTo(registry.activeModules());
    }

    @Transactional(readOnly = true)
    public boolean isActive(ModuleCode module) {
        return registry.activeModules().contains(module);
    }

    private ModuleActivation activationOf(ModuleDefinition definition) {
        ModuleCode module = definition.code();
        return activations.findByModuleCode(module.value()).orElseGet(() -> new ModuleActivation(module));
    }
}
