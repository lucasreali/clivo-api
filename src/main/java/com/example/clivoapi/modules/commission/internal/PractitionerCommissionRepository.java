package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.PractitionerCommission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerCommissionRepository extends JpaRepository<PractitionerCommission, UUID> {

    Optional<PractitionerCommission> findByPractitionerId(UUID practitionerId);
}
