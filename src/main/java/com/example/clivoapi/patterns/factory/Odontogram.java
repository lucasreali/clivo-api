package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;

final class Odontogram implements Field {

    private static final String PERMANENT = "permanent";

    private final MarkedRegions teeth;

    Odontogram(ChartCatalog catalogue, FieldDefinition definition) {
        this.teeth = new MarkedRegions(definition, catalogue.chartOf(definition, PERMANENT));
    }

    @Override
    public SheetField fill(RecordValues values) {
        return teeth.render(values);
    }

    @Override
    public void check(RecordValues values) {
        teeth.check(values);
    }
}
