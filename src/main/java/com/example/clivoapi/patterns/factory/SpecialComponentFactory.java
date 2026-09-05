package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component(SpecialComponentFactory.COMPONENT)
public class SpecialComponentFactory implements FieldFactory {

    public static final String COMPONENT = "COMPONENT";

    private final Map<String, ComponentFactory> components;
    private final ModuleActivationState activation;

    SpecialComponentFactory(Map<String, ComponentFactory> components, ModuleActivationState activation) {
        this.components = Map.copyOf(components);
        this.activation = activation;
    }

    @Override
    public Field create(FieldDefinition definition) {
        ComponentFactory factory = factoryFor(definition);
        requireModuleActive(factory, definition);
        return factory.create(definition);
    }

    private ComponentFactory factoryFor(FieldDefinition definition) {
        String named = definition.component().orElseThrow(() -> definition.refusal("must name a component"));
        return Optional.ofNullable(components.get(named))
                .orElseThrow(() -> definition.refusal("names the unknown component %s".formatted(named)));
    }

    private void requireModuleActive(ComponentFactory factory, FieldDefinition definition) {
        ModuleCode module = factory.requiredModule();
        if (activation.isActive(module)) {
            return;
        }
        throw definition.refusal("needs module %s, which is not active in this clinic".formatted(module));
    }
}
