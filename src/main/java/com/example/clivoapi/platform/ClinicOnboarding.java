package com.example.clivoapi.platform;

import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantService;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClinicOnboarding {

    private final TenantService clinics;
    private final AccessService access;

    ClinicOnboarding(TenantService clinics, AccessService access) {
        this.clinics = clinics;
        this.access = access;
    }

    public ProvisionedClinic open(NewClinic request) {
        Tenant clinic = clinics.create(request.clinic());
        UserSummary manager = access.registerIn(clinic, request.manager());
        return new ProvisionedClinic(clinic.snapshot(), manager);
    }
}
