package com.example.clivoapi.configuration.modules;

import com.example.clivoapi.common.extension.ModuleCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ActiveModules {

    private final Set<ModuleCode> codes;

    public ActiveModules(Collection<ModuleActivation> activations) {
        this.codes = activations.stream()
                .filter(ModuleActivation::isEnabled)
                .map(ModuleActivation::module)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean contains(ModuleCode module) {
        return codes.contains(module);
    }

    public boolean excludes(ModuleCode module) {
        return !contains(module);
    }

    public Optional<ModuleCode> firstOf(List<ModuleCode> candidates) {
        return candidates.stream().filter(this::contains).findFirst();
    }
}
