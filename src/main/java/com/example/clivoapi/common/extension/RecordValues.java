package com.example.clivoapi.common.extension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record RecordValues(Map<String, Object> values) {

    public RecordValues {
        values = Collections.unmodifiableMap(new LinkedHashMap<>(Optional.ofNullable(values).orElseGet(Map::of)));
    }

    public static RecordValues empty() {
        return new RecordValues(Map.of());
    }

    public static RecordValues of(Map<String, Object> values) {
        return new RecordValues(values);
    }

    public Optional<Object> valueOf(String code) {
        return Optional.ofNullable(values.get(code));
    }

    public boolean lacks(String code) {
        return valueOf(code).map(RecordValues::isEmptyText).orElse(true);
    }

    public Map<String, Object> asMap() {
        return values;
    }

    private static boolean isEmptyText(Object value) {
        return value instanceof String text && text.isBlank();
    }
}
