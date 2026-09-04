package com.example.clivoapi.configuration.capability;

import com.example.clivoapi.configuration.modules.ModuleDefinition;
import com.example.clivoapi.configuration.parameter.EffectiveParameter;
import java.util.List;

public record Capabilities(List<ModuleDefinition> modules, List<EffectiveParameter> parameters) {
}
