package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.tenant.internal.TenantRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TenantDirectory {

    private final TenantRepository tenants;
    private final TenantContext tenantContext;

    TenantDirectory(TenantRepository tenants, TenantContext tenantContext) {
        this.tenants = tenants;
        this.tenantContext = tenantContext;
    }

    public Optional<Tenant> current() {
        return tenantContext.current().flatMap(tenants::findById);
    }
}
