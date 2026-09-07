package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.InsurancePlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {

    List<InsurancePlan> findAllByOrderByNameAsc();
}
