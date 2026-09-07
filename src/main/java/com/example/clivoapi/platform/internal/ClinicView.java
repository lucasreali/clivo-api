package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.tenant.TenantIdentity;
import com.example.clivoapi.common.tenant.TenantLifecycle;
import com.example.clivoapi.common.tenant.TenantProfile;
import com.example.clivoapi.common.tenant.TenantSnapshot;
import java.time.Instant;
import java.util.UUID;

record ClinicView(
        UUID id,
        String code,
        String name,
        String legalName,
        String taxId,
        String segment,
        String status,
        String statusReason,
        Instant createdAt) {

    static ClinicView of(TenantSnapshot clinic) {
        return assembled(clinic.identity(), clinic.profile(), clinic.lifecycle(), clinic.createdAt());
    }

    private static ClinicView assembled(
            TenantIdentity identity, TenantProfile profile, TenantLifecycle lifecycle, Instant createdAt) {
        return new ClinicView(
                identity.id(),
                identity.code(),
                identity.name(),
                profile.legalName(),
                profile.taxId(),
                profile.segment(),
                lifecycle.status().name(),
                lifecycle.statusReason(),
                createdAt);
    }
}
