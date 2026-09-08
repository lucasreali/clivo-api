package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.math.BigDecimal;

public final class TextField implements Field {

    private static final String MAX_LENGTH = "maxLength";

    private final FieldDefinition definition;

    public TextField(FieldDefinition definition) {
        this.definition = definition;
    }

    @Override
    public SheetField fill(RecordValues values) {
        return definition.renderedWith(values);
    }

    @Override
    public void accept(RecordValues values) {
        definition.valueIn(values).ifPresent(this::checkText);
    }

    @Override
    public void check(RecordValues values) {
        definition.requirePresenceIn(values);
        accept(values);
    }

    private void checkText(Object value) {
        String text = asText(value);
        definition.rule(MAX_LENGTH).ifPresent(limit -> requireNoLongerThan(text, limit));
    }

    private String asText(Object value) {
        if (value instanceof String text) {
            return text;
        }
        throw definition.refusal("expects text");
    }

    private void requireNoLongerThan(String text, BigDecimal limit) {
        if (text.length() <= limit.intValue()) {
            return;
        }
        throw definition.refusal("accepts at most %d characters".formatted(limit.intValue()));
    }
}
