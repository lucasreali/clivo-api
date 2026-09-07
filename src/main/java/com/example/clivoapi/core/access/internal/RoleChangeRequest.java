package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.Role;
import jakarta.validation.constraints.NotNull;

record RoleChangeRequest(@NotNull Role role) {

    Role toRole() {
        return role;
    }
}
