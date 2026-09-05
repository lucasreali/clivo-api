package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Odontogram implements Field {

    private static final List<String> PERMANENT_TEETH = permanentTeeth();

    private final MarkedRegions teeth;

    public Odontogram(FieldDefinition definition) {
        this.teeth = new MarkedRegions(definition, PERMANENT_TEETH);
    }

    @Override
    public SheetField fill(RecordValues values) {
        return teeth.render(values);
    }

    @Override
    public void check(RecordValues values) {
        teeth.check(values);
    }

    private static List<String> permanentTeeth() {
        return IntStream.rangeClosed(1, 4).boxed().flatMap(Odontogram::quadrant).toList();
    }

    private static Stream<String> quadrant(int quadrant) {
        return IntStream.rangeClosed(1, 8).mapToObj(tooth -> "%d%d".formatted(quadrant, tooth));
    }
}
