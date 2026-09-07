package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ModuleGrantState;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class GrantedModuleAccess implements ModuleGrantState {

    private final ModuleGrantRepository grants;

    GrantedModuleAccess(ModuleGrantRepository grants) {
        this.grants = grants;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGrantedToCaller(ModuleCode module) {
        return SignedInCaller.current().filter(caller -> reaches(caller, module)).isPresent();
    }

    private boolean reaches(AuthenticatedUser caller, ModuleCode module) {
        return caller.hasRole(Role.MANAGER)
                || grants.existsByUserIdAndModuleCode(caller.userId(), module.value());
    }
}
