package com.example.clivoapi.configuration.parameter.internal;

import com.example.clivoapi.configuration.parameter.ClinicParameter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicParameterRepository extends JpaRepository<ClinicParameter, String> {

    Optional<ClinicParameter> findByParameterCode(String parameterCode);
}
