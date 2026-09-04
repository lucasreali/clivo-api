package com.example.clivoapi.common.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

class RequestTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    private static final Long NO_TENANT = 0L;

    private final TenantContext tenantContext;

    RequestTenantIdentifierResolver(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return tenantContext.current().orElse(NO_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
