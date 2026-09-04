package com.example.clivoapi.configuration.capability.internal;

import com.example.clivoapi.configuration.capability.Capabilities;
import com.example.clivoapi.configuration.modules.ModuleDefinition;
import com.example.clivoapi.configuration.parameter.EffectiveParameter;
import java.util.List;

record CapabilitiesView(List<ModuleView> modules, List<ParameterView> parameters) {

    static CapabilitiesView of(Capabilities capabilities) {
        return new CapabilitiesView(
                capabilities.modules().stream().map(ModuleView::of).toList(),
                capabilities.parameters().stream().map(ParameterView::of).toList());
    }

    record ModuleView(String code, String name) {

        static ModuleView of(ModuleDefinition definition) {
            return new ModuleView(definition.code().value(), definition.name());
        }
    }

    record ParameterView(String code, String value) {

        static ParameterView of(EffectiveParameter parameter) {
            return new ParameterView(parameter.code().value(), parameter.value().asText());
        }
    }
}
