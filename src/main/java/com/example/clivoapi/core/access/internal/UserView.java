package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.UserSummary;

record UserView(Long id, String name, String email, String role, boolean active) {

    static UserView of(UserSummary user) {
        return new UserView(user.id(), user.name(), user.email().asText(), user.role().name(), user.active());
    }
}
