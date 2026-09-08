package com.example.clivoapi.configuration.capability.internal;

import com.example.clivoapi.configuration.capability.Capabilities;
import com.example.clivoapi.configuration.modules.ModuleDefinition;
import com.example.clivoapi.configuration.parameter.EffectiveParameter;
import java.util.List;

record CapabilitiesView(List<ActiveModuleView> modules, List<EffectiveParameterView> parameters) {

    static CapabilitiesView of(Capabilities capabilities) {
        return new CapabilitiesView(
                capabilities.modules().stream().map(ActiveModuleView::of).toList(),
                capabilities.parameters().stream().map(EffectiveParameterView::of).toList());
    }

    record ActiveModuleView(String code, String name) {

        static ActiveModuleView of(ModuleDefinition definition) {
            return new ActiveModuleView(definition.code().value(), definition.name());
        }
    }

    record EffectiveParameterView(String code, String value) {

        static EffectiveParameterView of(EffectiveParameter parameter) {
            return new EffectiveParameterView(parameter.code().value(), parameter.value().asText());
        }
    }
}
