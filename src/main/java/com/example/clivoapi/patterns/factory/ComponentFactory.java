package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ModuleCode;

public interface ComponentFactory {

    ModuleCode requiredModule();

    Field create(FieldDefinition definition);
}
