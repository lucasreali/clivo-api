package com.example.clivoapi.common.tenant;

import java.util.UUID;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

class RequestTenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {

    private static final UUID NO_TENANT = new UUID(0, 0);

    private final TenantContext tenantContext;

    RequestTenantIdentifierResolver(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        return tenantContext.current().orElse(NO_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
