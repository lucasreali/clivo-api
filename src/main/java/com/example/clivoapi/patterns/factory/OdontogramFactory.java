package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ModuleCode;
import org.springframework.stereotype.Component;

@Component(OdontogramFactory.ODONTOGRAM)
public class OdontogramFactory implements ComponentFactory {

    public static final String ODONTOGRAM = "ODONTOGRAM";

    private static final ModuleCode MODULE = new ModuleCode("odontogram");

    private final ChartCatalog catalogue;

    OdontogramFactory(ChartCatalog catalogue) {
        this.catalogue = catalogue;
    }

    @Override
    public ModuleCode requiredModule() {
        return MODULE;
    }

    @Override
    public Field create(FieldDefinition definition) {
        return new Odontogram(catalogue, definition);
    }
}
