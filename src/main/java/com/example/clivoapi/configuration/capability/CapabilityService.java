package com.example.clivoapi.configuration.capability;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ModuleGrantState;
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
    private final ModuleGrantState grants;

    CapabilityService(
            ModuleActivationService modules, ClinicParameterService parameters, ModuleGrantState grants) {
        this.modules = modules;
        this.parameters = parameters;
        this.grants = grants;
    }

    public Capabilities current() {
        List<ModuleDefinition> reachable = reachableDefinitions();
        return new Capabilities(reachable, parametersWithin(codesOf(reachable)));
    }

    private List<ModuleDefinition> reachableDefinitions() {
        return modules.activeDefinitions().stream()
                .filter(definition -> grants.isGrantedToCaller(definition.code()))
                .toList();
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
