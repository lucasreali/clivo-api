package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ComponentRegion(String code, String label, Map<String, String> groups, int position, List<String> parts) {

    public ComponentRegion {
        groups = Map.copyOf(Optional.ofNullable(groups).orElseGet(Map::of));
        parts = List.copyOf(Optional.ofNullable(parts).orElseGet(List::of));
    }

    public static ComponentRegion named(String code, int position) {
        return new ComponentRegion(code, code, Map.of(), position, List.of());
    }

    public boolean has(String part) {
        return parts.contains(part);
    }
}
