package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.ComponentRegion;
import com.example.clivoapi.patterns.factory.internal.DeclaredChartRepository;
import com.example.clivoapi.patterns.factory.internal.DeclaredMarkRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
class ChartCatalog {

    private static final String VARIANT = "variant";

    private final DeclaredChartRepository charts;
    private final DeclaredMarkRepository marks;

    ChartCatalog(DeclaredChartRepository charts, DeclaredMarkRepository marks) {
        this.charts = charts;
        this.marks = marks;
    }

    ComponentDescriptor chartOf(FieldDefinition definition, String fallbackVariant) {
        String component = componentOf(definition);
        String variant = definition.setting(VARIANT).orElse(fallbackVariant);
        return charts.findByComponentAndVariant(component, variant)
                .map(chart -> chart.describedWith(vocabularyOf(component)))
                .orElseThrow(() -> definition.refusal("does not know the variant %s".formatted(variant)));
    }

    ComponentDescriptor regionsOf(FieldDefinition definition) {
        String component = componentOf(definition);
        return new ComponentDescriptor(
                component, List.of(), declaredRegionsOf(definition), vocabularyOf(component));
    }

    private List<ComponentRegion> declaredRegionsOf(FieldDefinition definition) {
        List<String> declared = definition.declaredOptions();
        return IntStream.range(0, declared.size())
                .mapToObj(index -> ComponentRegion.named(declared.get(index), index + 1))
                .toList();
    }

    private List<ComponentMark> vocabularyOf(String component) {
        return marks.findByComponentOrderBySortOrder(component).stream()
                .map(DeclaredMark::mark)
                .toList();
    }

    private String componentOf(FieldDefinition definition) {
        return definition.component().orElseGet(definition::fieldType);
    }
}
