package com.example.clivoapi.support;

import com.example.clivoapi.common.tenant.Tenant;

public record Clinic(Tenant tenant, Long customerId, Long practitionerId, Long serviceId) {

    public Long id() {
        return tenant.id();
    }
}
