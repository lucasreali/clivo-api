package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.CustomerInsurance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerInsuranceRepository extends JpaRepository<CustomerInsurance, UUID> {

    List<CustomerInsurance> findByCustomerIdOrderByIdAsc(UUID customerId);

    Optional<CustomerInsurance> findByCustomerIdAndPlanId(UUID customerId, UUID planId);
}
