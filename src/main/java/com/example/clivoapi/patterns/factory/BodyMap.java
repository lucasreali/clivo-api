package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;

public final class BodyMap implements Field {

    private final MarkedRegions regions;

    public BodyMap(FieldDefinition definition) {
        this.regions = new MarkedRegions(definition, definition.declaredOptions());
    }

    @Override
    public SheetField fill(RecordValues values) {
        return regions.render(values);
    }

    @Override
    public void check(RecordValues values) {
        regions.check(values);
    }
}
