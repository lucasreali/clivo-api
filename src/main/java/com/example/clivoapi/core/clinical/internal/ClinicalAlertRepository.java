package com.example.clivoapi.core.clinical.internal;

import com.example.clivoapi.core.clinical.ClinicalAlert;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalAlertRepository extends JpaRepository<ClinicalAlert, UUID> {

    List<ClinicalAlert> findByCustomerIdOrderByRecordedAtDesc(UUID customerId);
}
