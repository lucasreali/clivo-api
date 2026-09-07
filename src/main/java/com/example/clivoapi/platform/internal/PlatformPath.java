package com.example.clivoapi.platform.internal;

final class PlatformPath {

    static final String ROOT = "/api/platform";

    static final String CLINICS = ROOT + "/tenants";

    static final String ONE_CLINIC = CLINICS + "/{tenantId}";

    static final String CLINIC_VARIABLE = "tenantId";

    private PlatformPath() {
    }
}
