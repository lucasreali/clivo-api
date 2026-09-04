package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleActivationProposal;
import com.example.clivoapi.common.extension.ModuleActivationValidator;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ActiveModules;
import com.example.clivoapi.configuration.modules.ModuleRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(ModuleDependencyValidator.ORDER)
public class ModuleDependencyValidator implements ModuleActivationValidator {

    public static final int ORDER = 10;

    private final ModuleRegistry registry;

    ModuleDependencyValidator(ModuleRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void validate(ModuleActivationProposal proposal) {
        if (proposal.isDeactivation()) {
            return;
        }
        ActiveModules active = registry.activeModules();
        registry.definitionOf(proposal.module()).dependency()
                .filter(active::excludes)
                .ifPresent(dependency -> refuse(proposal.module(), dependency));
    }

    private void refuse(ModuleCode module, ModuleCode dependency) {
        throw new BusinessException("module %s requires module %s to be active".formatted(module, dependency));
    }
}
