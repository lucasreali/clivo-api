package com.example.clivoapi.patterns.factory;

import org.springframework.stereotype.Component;

@Component(TextFieldFactory.SHORT_TEXT)
public class TextFieldFactory implements FieldFactory {

    public static final String SHORT_TEXT = "SHORT_TEXT";

    @Override
    public Field create(FieldDefinition definition) {
        return new TextField(definition);
    }
}
