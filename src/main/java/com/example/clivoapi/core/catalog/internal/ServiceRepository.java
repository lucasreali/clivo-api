package com.example.clivoapi.core.catalog.internal;

import com.example.clivoapi.core.catalog.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByNameIgnoreCase(String name);

    List<Service> findAllByOrderByNameAsc();
}
