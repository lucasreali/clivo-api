package com.example.clivoapi.core.access;

import com.example.clivoapi.common.extension.ViewerRole;

public enum Role {

    RECEPTION,
    PRACTITIONER,
    ASSISTANT,
    MANAGER,
    PLATFORM_ADMIN;

    public boolean belongsToClinic() {
        return this != PLATFORM_ADMIN;
    }

    public boolean canCreate(Role requested) {
        return switch (this) {
            case PLATFORM_ADMIN -> requested == PLATFORM_ADMIN;
            case MANAGER -> requested.belongsToClinic();
            case RECEPTION, PRACTITIONER, ASSISTANT -> false;
        };
    }

    public boolean canAssign(Role requested) {
        return canCreate(requested);
    }

    public boolean manages() {
        return this == MANAGER;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    public ViewerRole asViewer() {
        return new ViewerRole(name());
    }
}
