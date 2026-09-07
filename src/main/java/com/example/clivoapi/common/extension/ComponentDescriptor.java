package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.Optional;

public record ComponentDescriptor(
        String component, List<String> groupings, List<ComponentRegion> regions, List<ComponentMark> vocabulary) {

    public ComponentDescriptor {
        groupings = List.copyOf(groupings);
        regions = List.copyOf(regions);
        vocabulary = List.copyOf(vocabulary);
    }

    public List<String> codes() {
        return regions.stream().map(ComponentRegion::code).toList();
    }

    public Optional<ComponentRegion> regionNamed(String code) {
        return regions.stream().filter(region -> region.code().equals(code)).findFirst();
    }

    public boolean accepts(String mark) {
        return vocabulary.stream().map(ComponentMark::code).anyMatch(mark::equals);
    }
}
