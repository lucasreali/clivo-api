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

    public String authority() {
        return "ROLE_" + name();
    }

    public ViewerRole asViewer() {
        return new ViewerRole(name());
    }
}
