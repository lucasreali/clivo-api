package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.util.List;

public final class SelectField implements Field {

    private final FieldDefinition definition;

    public SelectField(FieldDefinition definition) {
        this.definition = definition;
    }

    @Override
    public SheetField fill(RecordValues values) {
        return definition.renderedWith(values);
    }

    @Override
    public void check(RecordValues values) {
        definition.requirePresenceIn(values);
        definition.valueIn(values).ifPresent(this::checkChoice);
    }

    private void checkChoice(Object value) {
        List<String> accepted = definition.declaredOptions();
        if (accepted.contains(value.toString())) {
            return;
        }
        throw definition.refusal("accepts only one of %s".formatted(accepted));
    }
}
