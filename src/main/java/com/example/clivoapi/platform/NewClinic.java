package com.example.clivoapi.platform;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.TenantRegistration;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;

public record NewClinic(TenantRegistration clinic, UserRegistration manager) {

    public NewClinic {
        requireManager(manager);
    }

    private static void requireManager(UserRegistration manager) {
        if (manager.role() == Role.MANAGER) {
            return;
        }
        throw new BusinessException("a clinic opens with a manager, not a %s".formatted(manager.role()));
    }
}
