package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ModuleCode;
import org.springframework.stereotype.Component;

@Component(BodyMapFactory.BODY_MAP)
public class BodyMapFactory implements ComponentFactory {

    public static final String BODY_MAP = "BODY_MAP";

    private static final ModuleCode MODULE = new ModuleCode("bodymap");

    @Override
    public ModuleCode requiredModule() {
        return MODULE;
    }

    @Override
    public Field create(FieldDefinition definition) {
        return new BodyMap(definition);
    }
}
