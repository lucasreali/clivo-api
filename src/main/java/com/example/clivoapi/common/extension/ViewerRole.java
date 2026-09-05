package com.example.clivoapi.common.extension;

import java.util.Set;

public record ViewerRole(String value) {

    public ViewerRole {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a viewer role is required");
        }
    }

    public boolean isAnyOf(Set<String> roles) {
        return roles.contains(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
