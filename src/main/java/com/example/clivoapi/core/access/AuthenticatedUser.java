package com.example.clivoapi.core.access;

import com.example.clivoapi.common.audit.AuditorIdentity;
import java.util.Optional;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID clinicId, String name, Role role) implements AuditorIdentity {

    public Optional<UUID> clinic() {
        return Optional.ofNullable(clinicId);
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }
}
