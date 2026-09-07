package com.example.clivoapi.common.tenant;

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
        requireCodeAvailable(registration.code());
        return tenants.save(new Tenant(registration));
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

    private void requireCodeAvailable(String code) {
        if (tenants.findByCode(code).isEmpty()) {
            return;
        }
        throw new BusinessException("clinic code %s is already registered".formatted(code));
    }
}
