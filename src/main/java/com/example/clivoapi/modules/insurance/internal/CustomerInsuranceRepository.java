package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.CustomerInsurance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerInsuranceRepository extends JpaRepository<CustomerInsurance, Long> {

    List<CustomerInsurance> findByCustomerIdOrderByIdAsc(Long customerId);

    Optional<CustomerInsurance> findByCustomerIdAndPlanId(Long customerId, Long planId);
}
