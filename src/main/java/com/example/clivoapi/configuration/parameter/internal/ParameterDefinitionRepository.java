package com.example.clivoapi.configuration.parameter.internal;

import com.example.clivoapi.configuration.parameter.ParameterDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterDefinitionRepository extends JpaRepository<ParameterDefinition, String> {

    List<ParameterDefinition> findAllByOrderByCodeAsc();
}
