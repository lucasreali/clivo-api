package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.document.TaxId;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.tenant.internal.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantService {

    private final TenantRepository tenants;

    TenantService(TenantRepository tenants) {
        this.tenants = tenants;
    }

    public Tenant create(TenantRegistration registration) {
        Tenant clinic = new Tenant(registration);
        requireTaxIdAvailable(registration.profile().registeredTaxId().orElse(null), clinic);
        return tenants.save(clinic);
    }

    @Transactional(readOnly = true)
    public Tenant findOne(UUID clinicId) {
        return tenants.findById(clinicId).orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));
    }

    @Transactional(readOnly = true)
    public List<Tenant> list() {
        return tenants.findAllByOrderByNameAsc();
    }

    public Tenant describe(UUID clinicId, TenantDetails details) {
        Tenant clinic = findOne(clinicId);
        requireTaxIdAvailable(details.profile().registeredTaxId().orElse(null), clinic);
        clinic.describeAs(details);
        return tenants.save(clinic);
    }

    public Tenant activate(UUID clinicId) {
        Tenant clinic = findOne(clinicId);
        clinic.activate();
        return tenants.save(clinic);
    }

    public Tenant deactivate(UUID clinicId, String reason) {
        Tenant clinic = findOne(clinicId);
        clinic.deactivate(reason);
        return tenants.save(clinic);
    }

    private void requireTaxIdAvailable(TaxId taxId, Tenant clinic) {
        if (taxId == null) {
            return;
        }
        tenants.findByProfileTaxId(taxId)
                .filter(holder -> !holder.id().equals(clinic.id()))
                .ifPresent(holder -> refuseTaxIdOf(taxId, holder));
    }

    private void refuseTaxIdOf(TaxId taxId, Tenant holder) {
        throw new BusinessException(
                "taxId: %s already belongs to %s".formatted(taxId.asText(), holder.identity().name()));
    }
}
