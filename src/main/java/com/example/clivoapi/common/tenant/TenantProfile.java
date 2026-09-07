package com.example.clivoapi.common.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record TenantProfile(
        @Column(name = "legal_name", length = 160) String legalName,
        @Column(name = "tax_id", length = 14) String taxId,
        @Column(name = "segment", length = 60) String segment) {

    public static TenantProfile unknown() {
        return new TenantProfile(null, null, null);
    }
}
