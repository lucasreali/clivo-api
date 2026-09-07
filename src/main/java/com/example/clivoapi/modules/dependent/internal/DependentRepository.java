package com.example.clivoapi.modules.dependent.internal;

import com.example.clivoapi.modules.dependent.Dependent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependentRepository extends JpaRepository<Dependent, UUID> {

    List<Dependent> findByCustomerIdOrderByNameAsc(UUID customerId);
}
