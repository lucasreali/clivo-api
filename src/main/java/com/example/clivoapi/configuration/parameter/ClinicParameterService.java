package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.internal.ClinicParameterRepository;
import com.example.clivoapi.configuration.parameter.internal.ParameterDefinitionRepository;
import java.util.List;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClinicParameterService {

    private final ParameterDefinitionRepository definitions;
    private final ClinicParameterRepository parameters;
    private final AuditorAware<Long> auditor;

    ClinicParameterService(
            ParameterDefinitionRepository definitions,
            ClinicParameterRepository parameters,
            AuditorAware<Long> auditor) {
        this.definitions = definitions;
        this.parameters = parameters;
        this.auditor = auditor;
    }

    @Transactional(readOnly = true)
    public List<EffectiveParameter> effectiveParameters() {
        ParameterSettings settings = settings();
        return definitions.findAllByOrderByCodeAsc().stream()
                .map(definition -> EffectiveParameter.of(definition, settings.valueOf(definition)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ParameterValue valueOf(ParameterCode parameter) {
        return settings().valueOf(definitionOf(parameter));
    }

    public void change(ParameterCode parameter, ParameterValue value) {
        definitionOf(parameter).requireAcceptable(value);
        ClinicParameter stored = storedOf(parameter);
        stored.changeTo(value, auditor.getCurrentAuditor().orElse(null));
        parameters.save(stored);
    }

    private ParameterDefinition definitionOf(ParameterCode parameter) {
        return definitions.findById(parameter.value())
                .orElseThrow(() -> new ResourceNotFoundException("Parameter", parameter));
    }

    private ClinicParameter storedOf(ParameterCode parameter) {
        return parameters.findByParameterCode(parameter.value())
                .orElseGet(() -> new ClinicParameter(parameter));
    }

    private ParameterSettings settings() {
        return new ParameterSettings(parameters.findAll());
    }
}
