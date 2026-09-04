package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import java.util.Optional;

public record EffectiveParameter(ParameterCode code, String name, ParameterValue value, String acceptedValues, ModuleCode module) {

    static EffectiveParameter of(ParameterDefinition definition, ParameterValue value) {
        return new EffectiveParameter(
                definition.code(),
                definition.name(),
                value,
                definition.acceptedValues(),
                definition.module().orElse(null));
    }

    public Optional<ModuleCode> requiredModule() {
        return Optional.ofNullable(module);
    }
}
