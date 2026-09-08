package com.example.clivoapi.common.extension;

import java.util.List;

public record MarkedPart(String region, String part) {

    public static List<MarkedPart> covering(RegionMarking marking) {
        if (marking.coversTheWholeRegion()) {
            return List.of(new MarkedPart(marking.region(), null));
        }
        return marking.markedParts().stream()
                .map(part -> new MarkedPart(marking.region(), part))
                .toList();
    }

    public String describe() {
        if (part == null) {
            return "the region %s".formatted(region);
        }
        return "the part %s of the region %s".formatted(part, region);
    }
}
