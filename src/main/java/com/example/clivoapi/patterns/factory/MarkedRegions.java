package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.util.List;
import java.util.Map;

final class MarkedRegions {

    private final FieldDefinition definition;
    private final List<String> regions;

    MarkedRegions(FieldDefinition definition, List<String> regions) {
        this.definition = definition;
        this.regions = List.copyOf(regions);
    }

    SheetField render(RecordValues values) {
        return definition.renderedAs(componentName(), values, regions);
    }

    void check(RecordValues values) {
        definition.requirePresenceIn(values);
        definition.valueIn(values).ifPresent(this::checkMarks);
    }

    private void checkMarks(Object value) {
        asChart(value).keySet().forEach(this::requireKnown);
    }

    private Map<?, ?> asChart(Object value) {
        if (value instanceof Map<?, ?> chart) {
            return chart;
        }
        throw definition.refusal("expects a note for each marked region");
    }

    private void requireKnown(Object region) {
        if (regions.contains(region.toString())) {
            return;
        }
        throw definition.refusal("does not know the region %s".formatted(region));
    }

    private String componentName() {
        return definition.component().orElseGet(definition::fieldType);
    }
}
