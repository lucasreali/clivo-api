package com.example.clivoapi.core.access;

import com.example.clivoapi.common.audit.AuditorIdentity;
import com.example.clivoapi.common.tenant.TenantBoundPrincipal;
import java.util.Optional;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID clinicId, String name, Role role)
        implements AuditorIdentity, TenantBoundPrincipal {

    public Optional<UUID> clinic() {
        return Optional.ofNullable(clinicId);
    }

    @Override
    public Optional<UUID> tenant() {
        return clinic();
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }
}
