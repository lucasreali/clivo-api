package com.example.clivoapi.common.tenant.internal;

import com.example.clivoapi.common.document.TaxId;
import com.example.clivoapi.common.tenant.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByProfileTaxId(TaxId taxId);

    List<Tenant> findAllByOrderByNameAsc();
}
