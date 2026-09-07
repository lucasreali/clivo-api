package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.Encounter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

    List<Encounter> findByCustomerIdOrderByStartedAtDesc(UUID customerId);
}
