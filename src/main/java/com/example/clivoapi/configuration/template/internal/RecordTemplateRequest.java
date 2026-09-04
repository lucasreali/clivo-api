package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

record RecordTemplateRequest(
        @NotBlank String name,
        String requiresModule,
        @NotNull @Valid List<SectionRequest> sections) {

    TemplateContent content() {
        return new TemplateContent(sections.stream().map(SectionRequest::content).toList());
    }

    record SectionRequest(@NotBlank String name, @NotNull @Valid List<FieldRequest> fields) {

        SectionContent content() {
            return new SectionContent(name, fields.stream().map(FieldRequest::content).toList());
        }
    }

    record FieldRequest(
            @NotBlank String code,
            @NotBlank String label,
            @NotBlank String fieldType,
            String component,
            boolean required,
            List<String> options,
            Map<String, Object> validation,
            String requiresModule) {

        FieldContent content() {
            return new FieldContent(code, label, fieldType, component, required, options, validation, requiresModule);
        }
    }
}
