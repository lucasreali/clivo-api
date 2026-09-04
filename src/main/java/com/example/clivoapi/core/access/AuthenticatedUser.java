package com.example.clivoapi.core.access;

import com.example.clivoapi.common.audit.AuditorIdentity;
import java.util.Optional;

public record AuthenticatedUser(Long userId, Long clinicId, String name, Role role) implements AuditorIdentity {

    public Optional<Long> clinic() {
        return Optional.ofNullable(clinicId);
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }
}
