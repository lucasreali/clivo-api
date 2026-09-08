package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.ComponentRegion;
import com.example.clivoapi.common.extension.MarkedPart;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.RegionMarking;
import com.example.clivoapi.common.extension.RegionMarkings;
import com.example.clivoapi.common.extension.SheetField;
import java.util.HashSet;
import java.util.Set;

final class MarkedRegions {

    private static final int NOTE_LIMIT = 400;

    private final FieldDefinition definition;
    private final ComponentDescriptor descriptor;

    MarkedRegions(FieldDefinition definition, ComponentDescriptor descriptor) {
        this.definition = definition;
        this.descriptor = descriptor;
    }

    SheetField render(RecordValues values) {
        return definition.renderedAs(descriptor, values);
    }

    void accept(RecordValues values) {
        definition.valueIn(values).ifPresent(this::checkMarkings);
    }

    void check(RecordValues values) {
        definition.requirePresenceIn(values);
        accept(values);
    }

    private void checkMarkings(Object value) {
        Set<MarkedPart> marked = new HashSet<>();
        markingsIn(value).markings().forEach(marking -> checkMarking(marking, marked));
    }

    private RegionMarkings markingsIn(Object value) {
        return RegionMarkings.in(value).orElseThrow(this::expectsMarkings);
    }

    private void checkMarking(RegionMarking marking, Set<MarkedPart> marked) {
        ComponentRegion region = regionNamed(marking.region());
        marking.markedParts().forEach(part -> requirePartOf(region, part));
        requireAcceptedBy(markNamed(marking.mark()), marking);
        marking.writtenNote().ifPresent(this::requireShortNote);
        MarkedPart.covering(marking).forEach(part -> requireUnmarked(part, marked));
    }

    private ComponentRegion regionNamed(String code) {
        if (code == null) {
            throw expectsARegion();
        }
        return descriptor
                .regionNamed(code)
                .orElseThrow(() -> definition.refusal("does not know the region %s".formatted(code)));
    }

    private ComponentMark markNamed(String code) {
        if (code == null) {
            throw expectsAMark();
        }
        return descriptor
                .markNamed(code)
                .orElseThrow(() -> definition.refusal("does not know the mark %s".formatted(code)));
    }

    private void requirePartOf(ComponentRegion region, String part) {
        if (region.has(part)) {
            return;
        }
        throw definition.refusal("does not know the part %s of the region %s".formatted(part, region.code()));
    }

    private void requireAcceptedBy(ComponentMark mark, RegionMarking marking) {
        if (mark.accepts(marking)) {
            return;
        }
        throw definition.refusal(
                "marks %s with %s, which applies to %s".formatted(
                        marking.region(), mark.code(), mark.appliesTo().demand()));
    }

    private void requireShortNote(String note) {
        if (note.length() <= NOTE_LIMIT) {
            return;
        }
        throw definition.refusal("accepts a note of at most %d characters on a marking".formatted(NOTE_LIMIT));
    }

    private void requireUnmarked(MarkedPart part, Set<MarkedPart> marked) {
        if (marked.add(part)) {
            return;
        }
        throw definition.refusal("marks %s twice".formatted(part.describe()));
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
