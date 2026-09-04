package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.configuration.modules.ModuleActivation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleActivationRepository extends JpaRepository<ModuleActivation, String> {

    Optional<ModuleActivation> findByModuleCode(String moduleCode);
}
