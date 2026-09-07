package com.example.clivoapi.common.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public void bind(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    public Optional<UUID> current() {
        return Optional.ofNullable(currentTenant.get());
    }

    public void clear() {
        currentTenant.remove();
    }
}
