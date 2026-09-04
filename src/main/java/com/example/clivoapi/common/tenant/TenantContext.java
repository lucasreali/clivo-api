package com.example.clivoapi.common.tenant;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public void bind(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public Optional<Long> current() {
        return Optional.ofNullable(currentTenant.get());
    }

    public void clear() {
        currentTenant.remove();
    }
}
