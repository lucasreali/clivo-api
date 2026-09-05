package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.PractitionerCommission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerCommissionRepository extends JpaRepository<PractitionerCommission, Long> {

    Optional<PractitionerCommission> findByPractitionerId(Long practitionerId);
}
