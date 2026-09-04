package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.extension.ParameterValue;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class ParameterSettings {

    private final Map<String, ClinicParameter> byCode;

    ParameterSettings(Collection<ClinicParameter> stored) {
        this.byCode = stored.stream().collect(Collectors.toMap(this::keyOf, Function.identity()));
    }

    ParameterValue valueOf(ParameterDefinition definition) {
        return Optional.ofNullable(byCode.get(definition.code().value()))
                .map(ClinicParameter::value)
                .orElseGet(definition::defaultValue);
    }

    private String keyOf(ClinicParameter parameter) {
        return parameter.code().value();
    }
}
