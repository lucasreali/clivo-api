package com.example.clivoapi.patterns.factory;

import org.springframework.stereotype.Component;

@Component(SelectFieldFactory.SINGLE_CHOICE)
public class SelectFieldFactory implements FieldFactory {

    public static final String SINGLE_CHOICE = "SINGLE_CHOICE";

    @Override
    public Field create(FieldDefinition definition) {
        return new SelectField(definition);
    }
}
