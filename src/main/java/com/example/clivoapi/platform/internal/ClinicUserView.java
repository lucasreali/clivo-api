package com.example.clivoapi.platform.internal;

import com.example.clivoapi.core.access.UserSummary;
import java.util.UUID;

record ClinicUserView(UUID id, String name, String email, String role, boolean active) {

    static ClinicUserView of(UserSummary user) {
        return new ClinicUserView(
                user.id(), user.name(), user.email().asText(), user.role().name(), user.active());
    }
}
