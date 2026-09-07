package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentRegion;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.util.List;
import java.util.Map;

final class MarkedRegions {

    private final FieldDefinition definition;
    private final ComponentDescriptor descriptor;

    MarkedRegions(FieldDefinition definition, ComponentDescriptor descriptor) {
        this.definition = definition;
        this.descriptor = descriptor;
    }

    SheetField render(RecordValues values) {
        return definition.renderedAs(descriptor, values);
    }

    void check(RecordValues values) {
        definition.requirePresenceIn(values);
        definition.valueIn(values).ifPresent(this::checkMarkings);
    }

    private void checkMarkings(Object value) {
        markingsIn(value).stream().map(this::markingOf).forEach(this::checkMarking);
    }

    private List<?> markingsIn(Object value) {
        if (value instanceof List<?> markings) {
            return markings;
        }
        throw expectsMarkings();
    }

    private Marking markingOf(Object entry) {
        if (entry instanceof Map<?, ?> fields) {
            return new Marking(fields);
        }
        throw expectsMarkings();
    }

    private void checkMarking(Marking marking) {
        ComponentRegion region = regionNamed(marking.region().orElseThrow(this::expectsARegion));
        marking.part().ifPresent(part -> requirePartOf(region, part));
        requireKnown(marking.mark().orElseThrow(this::expectsAMark));
    }

    private ComponentRegion regionNamed(String code) {
        return descriptor
                .regionNamed(code)
                .orElseThrow(() -> definition.refusal("does not know the region %s".formatted(code)));
    }

    private void requirePartOf(ComponentRegion region, String part) {
        if (region.has(part)) {
            return;
        }
        throw definition.refusal("does not know the part %s of the region %s".formatted(part, region.code()));
    }

    private void requireKnown(String mark) {
        if (descriptor.accepts(mark)) {
            return;
        }
        throw definition.refusal("does not know the mark %s".formatted(mark));
    }

    private BusinessException expectsMarkings() {
        return definition.refusal("expects a list of markings");
    }

    private BusinessException expectsARegion() {
        return definition.refusal("expects a region on every marking");
    }

    private BusinessException expectsAMark() {
        return definition.refusal("expects a mark on every marking");
    }
}
