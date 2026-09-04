package com.example.clivoapi.configuration.capability;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.configuration.modules.ModuleDefinition;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.configuration.parameter.EffectiveParameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CapabilityService {

    private final ModuleActivationService modules;
    private final ClinicParameterService parameters;

    CapabilityService(ModuleActivationService modules, ClinicParameterService parameters) {
        this.modules = modules;
        this.parameters = parameters;
    }

    public Capabilities current() {
        List<ModuleDefinition> active = modules.activeDefinitions();
        return new Capabilities(active, parametersWithin(codesOf(active)));
    }

    private List<EffectiveParameter> parametersWithin(Set<ModuleCode> active) {
        return parameters.effectiveParameters().stream()
                .filter(parameter -> isReachable(parameter, active))
                .toList();
    }

    private boolean isReachable(EffectiveParameter parameter, Set<ModuleCode> active) {
        return parameter.requiredModule().map(active::contains).orElse(true);
    }

    private Set<ModuleCode> codesOf(List<ModuleDefinition> definitions) {
        return definitions.stream().map(ModuleDefinition::code).collect(Collectors.toUnmodifiableSet());
    }
}
