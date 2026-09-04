package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;

record SessionView(Long userId, Long clinicId, String name, String role) {

    static SessionView of(AuthenticatedUser user) {
        return new SessionView(user.userId(), user.clinicId(), user.name(), user.role().name());
    }
}
