package com.example.clivoapi.support;

import com.example.clivoapi.common.tenant.Tenant;
import java.util.UUID;

public record Clinic(Tenant tenant, UUID customerId, UUID practitionerId, UUID serviceId) {

    public UUID id() {
        return tenant.id();
    }
}
