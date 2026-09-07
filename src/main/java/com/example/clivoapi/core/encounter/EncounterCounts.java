package com.example.clivoapi.core.encounter;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record EncounterCounts(Map<EncounterStatus, Integer> byStatus) {

    public EncounterCounts {
        byStatus = Map.copyOf(byStatus);
    }

    public static EncounterCounts of(List<HistoryEntry> entries) {
        return new EncounterCounts(Arrays.stream(EncounterStatus.values())
                .collect(toMap(identity(), status -> countIn(entries, status))));
    }

    public int total() {
        return byStatus.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int of(EncounterStatus status) {
        return byStatus.getOrDefault(status, 0);
    }

    private static int countIn(List<HistoryEntry> entries, EncounterStatus status) {
        return (int) entries.stream().filter(entry -> entry.hasStatus(status)).count();
    }
}
