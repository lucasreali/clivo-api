package com.example.clivoapi.modules.dependent.internal;

import com.example.clivoapi.modules.dependent.Dependent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByCustomerIdOrderByNameAsc(Long customerId);
}
