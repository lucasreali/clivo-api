package com.example.clivoapi.configuration.modules.internal;

import com.example.clivoapi.configuration.modules.ModuleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleDefinitionRepository extends JpaRepository<ModuleDefinition, String> {
}
