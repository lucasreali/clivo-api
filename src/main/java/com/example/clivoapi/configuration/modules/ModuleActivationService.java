package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.internal.ModuleActivationRepository;
import com.example.clivoapi.configuration.modules.internal.ModuleDefinitionRepository;
import java.util.List;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleActivationService {

    private final ModuleDefinitionRepository definitions;
    private final ModuleActivationRepository activations;
    private final AuditorAware<Long> auditor;

    ModuleActivationService(
            ModuleDefinitionRepository definitions,
            ModuleActivationRepository activations,
            AuditorAware<Long> auditor) {
        this.definitions = definitions;
        this.activations = activations;
        this.auditor = auditor;
    }

    public void activate(ModuleCode module) {
        ModuleDefinition definition = catalog().definitionOf(module);
        requireDependencyActive(definition);
        ModuleActivation activation = activationOf(module);
        activation.enable(auditor.getCurrentAuditor().orElse(null));
        activations.save(activation);
    }

    public void deactivate(ModuleCode module) {
        ModuleCatalog catalog = catalog();
        ModuleDefinition definition = catalog.definitionOf(module);
        requireNoActiveDependents(catalog, definition.code());
        ModuleActivation activation = activationOf(module);
        activation.disable();
        activations.save(activation);
    }

    @Transactional(readOnly = true)
    public List<ModuleStatus> statusOfAll() {
        return catalog().statusWithin(activeModules());
    }

    @Transactional(readOnly = true)
    public List<ModuleDefinition> activeDefinitions() {
        return catalog().restrictedTo(activeModules());
    }

    @Transactional(readOnly = true)
    public boolean isActive(ModuleCode module) {
        return activeModules().contains(module);
    }

    private void requireDependencyActive(ModuleDefinition definition) {
        ActiveModules active = activeModules();
        definition.dependency()
                .filter(active::excludes)
                .ifPresent(dependency -> refuseMissingDependency(definition.code(), dependency));
    }

    private void requireNoActiveDependents(ModuleCatalog catalog, ModuleCode module) {
        activeModules()
                .firstOf(catalog.dependentsOf(module))
                .ifPresent(dependent -> refuseActiveDependent(module, dependent));
    }

    private void refuseMissingDependency(ModuleCode module, ModuleCode dependency) {
        throw new BusinessException("module %s requires module %s to be active".formatted(module, dependency));
    }

    private void refuseActiveDependent(ModuleCode module, ModuleCode dependent) {
        throw new BusinessException(
                "module %s cannot be deactivated while module %s is active".formatted(module, dependent));
    }

    private ModuleActivation activationOf(ModuleCode module) {
        return activations.findByModuleCode(module.value()).orElseGet(() -> new ModuleActivation(module));
    }

    private ModuleCatalog catalog() {
        return new ModuleCatalog(definitions.findAll());
    }

    private ActiveModules activeModules() {
        return new ActiveModules(activations.findAll());
    }
}
