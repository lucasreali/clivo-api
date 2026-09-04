package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleActivationProposal;
import com.example.clivoapi.common.extension.ModuleActivationValidator;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(ExistingDataValidator.ORDER)
public class ExistingDataValidator implements ModuleActivationValidator {

    public static final int ORDER = 20;

    private final ModuleRegistry registry;

    ExistingDataValidator(ModuleRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void validate(ModuleActivationProposal proposal) {
        if (proposal.isActivation()) {
            return;
        }
        ModuleCode module = proposal.module();
        registry.activeModules()
                .firstOf(registry.dependentsOf(module))
                .ifPresent(dependent -> refuse(module, dependent));
    }

    private void refuse(ModuleCode module, ModuleCode dependent) {
        throw new BusinessException(
                "module %s cannot be deactivated while module %s is active".formatted(module, dependent));
    }
}
