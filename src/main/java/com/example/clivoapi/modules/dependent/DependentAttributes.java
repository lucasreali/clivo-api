package com.example.clivoapi.modules.dependent;

import java.util.Map;
import java.util.Optional;

public record DependentAttributes(Map<String, Object> values) {

    public DependentAttributes {
        values = Map.copyOf(Optional.ofNullable(values).orElseGet(Map::of));
    }

    public static DependentAttributes none() {
        return new DependentAttributes(Map.of());
    }

    public Map<String, Object> asMap() {
        return values;
    }
}
