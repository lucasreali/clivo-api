package com.example.clivoapi.patterns.factory;

import org.springframework.stereotype.Component;

@Component(DateFieldFactory.DATE)
public class DateFieldFactory implements FieldFactory {

    public static final String DATE = "DATE";

    @Override
    public Field create(FieldDefinition definition) {
        return new DateField(definition);
    }
}
