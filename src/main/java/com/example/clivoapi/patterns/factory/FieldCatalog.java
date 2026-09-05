package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.configuration.template.SectionContent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FieldCatalog {

    private final Map<String, FieldFactory> factories;
    private final ModuleActivationState activation;

    FieldCatalog(Map<String, FieldFactory> factories, ModuleActivationState activation) {
        this.factories = Map.copyOf(factories);
        this.activation = activation;
    }

    public List<Field> fieldsOf(SectionContent section) {
        return section.fields().stream()
                .map(FieldDefinition::new)
                .filter(this::isAvailable)
                .map(this::fieldFor)
                .toList();
    }

    private boolean isAvailable(FieldDefinition definition) {
        return definition.requiredModule().map(activation::isActive).orElse(true);
    }

    private Field fieldFor(FieldDefinition definition) {
        return factoryFor(definition).create(definition);
    }

    private FieldFactory factoryFor(FieldDefinition definition) {
        return Optional.ofNullable(factories.get(definition.fieldType()))
                .orElseThrow(() -> definition.refusal("declares the unknown type %s".formatted(definition.fieldType())));
    }
}
