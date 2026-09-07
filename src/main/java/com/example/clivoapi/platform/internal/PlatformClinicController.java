package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantService;
import com.example.clivoapi.platform.ClinicOnboarding;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PlatformPath.CLINICS)
@Tag(name = "Platform clinics", description = "Onboarding and administration of the clinics on the platform")
class PlatformClinicController {

    private final ClinicOnboarding onboarding;
    private final TenantService clinics;

    PlatformClinicController(ClinicOnboarding onboarding, TenantService clinics) {
        this.onboarding = onboarding;
        this.clinics = clinics;
    }

    @Operation(operationId = "provisionClinic", summary = "Open a clinic together with its first manager")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProvisionedClinicView provision(@Valid @RequestBody NewClinicRequest request) {
        return ProvisionedClinicView.of(onboarding.open(request.toNewClinic()));
    }

    @Operation(operationId = "listClinics", summary = "List every clinic on the platform")
    @GetMapping
    List<ClinicView> list() {
        return clinics.list().stream().map(Tenant::snapshot).map(ClinicView::of).toList();
    }

    @Operation(operationId = "getClinic", summary = "Describe one clinic")
    @GetMapping("/{tenantId}")
    ClinicView findOne(@PathVariable UUID tenantId) {
        return viewOf(clinics.findOne(tenantId));
    }

    @Operation(operationId = "updateClinic", summary = "Change the details of one clinic")
    @PutMapping("/{tenantId}")
    ClinicView update(@PathVariable UUID tenantId, @Valid @RequestBody ClinicDetailsRequest request) {
        return viewOf(clinics.describe(tenantId, request.toDetails()));
    }

    @Operation(operationId = "activateClinic", summary = "Put a clinic back in service")
    @PutMapping("/{tenantId}/activation")
    ClinicView activate(@PathVariable UUID tenantId) {
        return viewOf(clinics.activate(tenantId));
    }

    @Operation(operationId = "deactivateClinic", summary = "Take a clinic out of service, keeping its records")
    @PutMapping("/{tenantId}/deactivation")
    ClinicView deactivate(@PathVariable UUID tenantId, @Valid @RequestBody DeactivationRequest request) {
        return viewOf(clinics.deactivate(tenantId, request.reason()));
    }

    private ClinicView viewOf(Tenant clinic) {
        return ClinicView.of(clinic.snapshot());
    }
}
