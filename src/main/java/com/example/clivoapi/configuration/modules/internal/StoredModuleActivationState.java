package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import org.springframework.stereotype.Component;

@Component
class StoredModuleActivationState implements ModuleActivationState {

    private final ModuleActivationService modules;

    StoredModuleActivationState(ModuleActivationService modules) {
        this.modules = modules;
    }

    @Override
    public boolean isActive(ModuleCode module) {
        return modules.isActive(module);
    }
}
