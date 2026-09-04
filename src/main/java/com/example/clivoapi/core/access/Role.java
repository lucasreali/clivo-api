package com.example.clivoapi.core.access;

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
}
