package com.example.clivoapi.patterns.factory;

import org.springframework.stereotype.Component;

@Component(NumberFieldFactory.INTEGER)
public class NumberFieldFactory implements FieldFactory {

    public static final String INTEGER = "INTEGER";

    @Override
    public Field create(FieldDefinition definition) {
        return new NumberField(definition);
    }
}
