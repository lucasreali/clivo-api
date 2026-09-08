package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DateField implements Field {

    private final FieldDefinition definition;

    public DateField(FieldDefinition definition) {
        this.definition = definition;
    }

    @Override
    public SheetField fill(RecordValues values) {
        return definition.renderedWith(values);
    }

    @Override
    public void accept(RecordValues values) {
        definition.valueIn(values).ifPresent(this::checkDate);
    }

    @Override
    public void check(RecordValues values) {
        definition.requirePresenceIn(values);
        accept(values);
    }

    private void checkDate(Object value) {
        try {
            LocalDate.parse(value.toString().trim());
        } catch (DateTimeParseException notADate) {
            throw definition.refusal("expects a date written as yyyy-MM-dd");
        }
    }
}
