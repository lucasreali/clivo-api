package com.example.clivoapi.core.access;

import com.example.clivoapi.common.tenant.TenantIdentity;
import java.util.Optional;

public record SignedInSession(AuthenticatedUser user, TenantIdentity clinic) {

    public Optional<TenantIdentity> clinicIdentity() {
        return Optional.ofNullable(clinic);
    }
}
