package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.Practitioner;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerRepository extends JpaRepository<Practitioner, UUID> {

    List<Practitioner> findAllByOrderByNameAsc();
}
