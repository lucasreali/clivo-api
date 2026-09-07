package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.tenant.TenantDetails;
import com.example.clivoapi.common.tenant.TenantProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ClinicDetailsRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 160) String legalName,
        @Size(max = 14) String taxId,
        @Size(max = 60) String segment) {

    TenantDetails toDetails() {
        return new TenantDetails(name, new TenantProfile(legalName, taxId, segment));
    }
}
