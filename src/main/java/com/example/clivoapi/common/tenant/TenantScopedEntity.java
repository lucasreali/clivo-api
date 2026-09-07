package com.example.clivoapi.common.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
public abstract class TenantScopedEntity {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    public boolean belongsTo(Tenant tenant) {
        return tenantId.equals(tenant.id());
    }
}
