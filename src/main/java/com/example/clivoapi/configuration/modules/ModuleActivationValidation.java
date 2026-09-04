package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleActivationProposal;
import com.example.clivoapi.common.extension.ModuleActivationValidator;
import java.util.List;

public class ModuleActivationValidation {

    private final List<ModuleActivationValidator> validators;

    public ModuleActivationValidation(List<ModuleActivationValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    public void check(ModuleActivationProposal proposal) {
        validators.forEach(validator -> validator.validate(proposal));
    }
}
