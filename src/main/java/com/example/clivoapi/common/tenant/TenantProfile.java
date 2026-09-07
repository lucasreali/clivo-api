package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.document.TaxId;
import com.example.clivoapi.common.text.TextField;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Optional;

@Embeddable
public record TenantProfile(
        @Column(name = "legal_name", length = 160) String legalName,
        @Embedded TaxId taxId,
        @Column(name = "segment", length = 60) String segment) {

    private static final TextField LEGAL_NAME = new TextField("a legal name", 160);
    private static final TextField SEGMENT = new TextField("a segment", 60);

    public TenantProfile {
        legalName = LEGAL_NAME.optional(legalName);
        segment = SEGMENT.optional(segment);
    }

    public static TenantProfile unknown() {
        return new TenantProfile(null, null, null);
    }

    public Optional<TaxId> registeredTaxId() {
        return Optional.ofNullable(taxId);
    }
}
