package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import java.math.BigDecimal;

public final class NumberField implements Field {

    private static final String MINIMUM = "min";

    private static final String MAXIMUM = "max";

    private final FieldDefinition definition;

    public NumberField(FieldDefinition definition) {
        this.definition = definition;
    }

    @Override
    public SheetField fill(RecordValues values) {
        return definition.renderedWith(values);
    }

    @Override
    public void check(RecordValues values) {
        definition.requirePresenceIn(values);
        definition.valueIn(values).ifPresent(this::checkNumber);
    }

    private void checkNumber(Object value) {
        BigDecimal number = asNumber(value);
        definition.rule(MINIMUM).ifPresent(minimum -> requireAtLeast(number, minimum));
        definition.rule(MAXIMUM).ifPresent(maximum -> requireAtMost(number, maximum));
    }

    private BigDecimal asNumber(Object value) {
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException notANumber) {
            throw definition.refusal("expects a number");
        }
    }

    private void requireAtLeast(BigDecimal number, BigDecimal minimum) {
        if (number.compareTo(minimum) >= 0) {
            return;
        }
        throw definition.refusal("accepts no value below %s".formatted(minimum.toPlainString()));
    }

    private void requireAtMost(BigDecimal number, BigDecimal maximum) {
        if (number.compareTo(maximum) <= 0) {
            return;
        }
        throw definition.refusal("accepts no value above %s".formatted(maximum.toPlainString()));
    }
}
