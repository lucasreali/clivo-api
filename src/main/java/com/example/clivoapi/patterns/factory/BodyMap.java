package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;

final class BodyMap implements Field {

    private final MarkedRegions regions;

    BodyMap(ChartCatalog catalogue, FieldDefinition definition) {
        this.regions = new MarkedRegions(definition, catalogue.regionsOf(definition));
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
