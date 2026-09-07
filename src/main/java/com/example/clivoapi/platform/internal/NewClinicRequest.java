package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.document.TaxId;
import com.example.clivoapi.common.tenant.TenantProfile;
import com.example.clivoapi.common.tenant.TenantRegistration;
import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;
import com.example.clivoapi.platform.NewClinic;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;

record NewClinicRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 160) String legalName,
        @Size(max = 18) String taxId,
        @Size(max = 60) String segment,
        @NotBlank @Size(max = 120) String managerName,
        @NotBlank @Email @Size(max = 160) String managerEmail,
        @NotBlank @Size(min = 8, max = 120) String managerPassword) {

    NewClinic toNewClinic() {
        return new NewClinic(new TenantRegistration(name, profile()), manager());
    }

    private TenantProfile profile() {
        return new TenantProfile(legalName, declaredTaxId().orElse(null), segment);
    }

    private Optional<TaxId> declaredTaxId() {
        return Optional.ofNullable(taxId).filter(value -> !value.isBlank()).map(TaxId::new);
    }

    private UserRegistration manager() {
        return new UserRegistration(
                managerName, new EmailAddress(managerEmail), new RawPassword(managerPassword), Role.MANAGER);
    }
}
