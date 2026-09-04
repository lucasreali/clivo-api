package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNationalIdValue(String nationalId);

    List<Customer> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Customer> findAllByOrderByNameAsc();
}
