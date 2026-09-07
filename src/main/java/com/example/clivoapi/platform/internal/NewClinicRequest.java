package com.example.clivoapi.platform.internal;

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

record NewClinicRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 160) String legalName,
        @Size(max = 14) String taxId,
        @Size(max = 60) String segment,
        @NotBlank String managerName,
        @NotBlank @Email String managerEmail,
        @NotBlank @Size(min = 8) String managerPassword) {

    NewClinic toNewClinic() {
        return new NewClinic(
                new TenantRegistration(code, name, new TenantProfile(legalName, taxId, segment)),
                new UserRegistration(
                        managerName,
                        new EmailAddress(managerEmail),
                        new RawPassword(managerPassword),
                        Role.MANAGER));
    }
}
