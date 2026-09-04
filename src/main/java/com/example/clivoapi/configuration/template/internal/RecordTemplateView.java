package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateSnapshot;
import java.util.List;
import java.util.Map;

record RecordTemplateView(
        Long id, String name, int version, String status, String requiresModule, List<SectionView> sections) {

    static RecordTemplateView of(TemplateSnapshot snapshot) {
        return new RecordTemplateView(
                snapshot.id(),
                snapshot.name(),
                snapshot.version(),
                snapshot.status().name(),
                textOf(snapshot.requiresModule()),
                snapshot.content().sections().stream().map(SectionView::of).toList());
    }

    private static String textOf(ModuleCode module) {
        return module == null ? null : module.value();
    }

    record SectionView(String name, List<FieldView> fields) {

        static SectionView of(SectionContent section) {
            return new SectionView(section.name(), section.fields().stream().map(FieldView::of).toList());
        }
    }

    record FieldView(
            String code,
            String label,
            String fieldType,
            String component,
            boolean required,
            List<String> options,
            Map<String, Object> validation,
            String requiresModule) {

        static FieldView of(FieldContent field) {
            return new FieldView(
                    field.code(),
                    field.label(),
                    field.fieldType(),
                    field.component(),
                    field.required(),
                    field.options(),
                    field.validation(),
                    field.requiresModule());
        }
    }
}
