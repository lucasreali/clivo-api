package com.example.clivoapi.common.tenant;

import java.time.Instant;

public record TenantSnapshot(
        TenantIdentity identity, TenantProfile profile, TenantLifecycle lifecycle, Instant createdAt) {
}
