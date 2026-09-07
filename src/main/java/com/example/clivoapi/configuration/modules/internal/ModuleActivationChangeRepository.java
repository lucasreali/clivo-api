package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.configuration.modules.ModuleActivationChange;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleActivationChangeRepository extends JpaRepository<ModuleActivationChange, UUID> {

    List<ModuleActivationChange> findAllByOrderByChangedAtDesc();
}
