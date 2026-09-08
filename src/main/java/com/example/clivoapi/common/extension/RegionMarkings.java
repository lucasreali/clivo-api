package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record RegionMarkings(List<RegionMarking> markings) {

    private static final String REGION = "region";

    private static final String PARTS = "parts";

    private static final String MARK = "mark";

    private static final String NOTE = "note";

    public RegionMarkings {
        markings = List.copyOf(markings);
    }

    public static Optional<RegionMarkings> in(Object value) {
        return listOf(value).filter(RegionMarkings::holdsOnlyEntries).map(RegionMarkings::readAll);
    }

    public boolean isEmpty() {
        return markings.isEmpty();
    }

    private static Optional<List<?>> listOf(Object value) {
        return Optional.ofNullable(value).filter(List.class::isInstance).map(List.class::cast);
    }

    private static boolean holdsOnlyEntries(List<?> entries) {
        return entries.stream().allMatch(Map.class::isInstance);
    }

    private static RegionMarkings readAll(List<?> entries) {
        return new RegionMarkings(
                entries.stream().map(Map.class::cast).map(RegionMarkings::read).toList());
    }

    private static RegionMarking read(Map<?, ?> entry) {
        return new RegionMarking(
                textAt(entry, REGION), partsAt(entry), textAt(entry, MARK), textAt(entry, NOTE));
    }

    private static List<String> partsAt(Map<?, ?> entry) {
        return listOf(entry.get(PARTS)).map(RegionMarkings::textsOf).orElseGet(List::of);
    }

    private static List<String> textsOf(List<?> parts) {
        return parts.stream().map(Object::toString).toList();
    }

    private static String textAt(Map<?, ?> entry, String key) {
        return Optional.ofNullable(entry.get(key)).map(Object::toString).orElse(null);
    }
}
