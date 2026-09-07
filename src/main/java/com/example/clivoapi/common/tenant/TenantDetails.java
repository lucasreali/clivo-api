package com.example.clivoapi.common.tenant;

import java.util.Objects;

public record TenantDetails(String name, TenantProfile profile) {

    public TenantDetails {
        name = TenantRegistration.requiredName(name);
        profile = Objects.requireNonNullElseGet(profile, TenantProfile::unknown);
    }
}
