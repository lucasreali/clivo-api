package com.example.clivoapi.platform.internal;

import com.example.clivoapi.configuration.parameter.EffectiveParameter;

record PlatformParameterView(String code, String name, String value, String accepts) {

    static PlatformParameterView of(EffectiveParameter parameter) {
        return new PlatformParameterView(
                parameter.code().value(),
                parameter.name(),
                parameter.value().asText(),
                parameter.acceptedValues());
    }
}
