package com.example.clivoapi.configuration.parameter.internal;

import com.example.clivoapi.configuration.parameter.EffectiveParameter;

record ParameterView(String code, String name, String value, String accepts) {

    static ParameterView of(EffectiveParameter parameter) {
        return new ParameterView(
                parameter.code().value(),
                parameter.name(),
                parameter.value().asText(),
                parameter.acceptedValues());
    }
}
