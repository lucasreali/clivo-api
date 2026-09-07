package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

record RecordTemplateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 4) String requiresModule,
        @NotNull @Valid List<SectionRequest> sections) {

    TemplateContent content() {
        return new TemplateContent(sections.stream().map(SectionRequest::content).toList());
    }

    record SectionRequest(@NotBlank @Size(max = 80) String name, @NotNull @Valid List<FieldRequest> fields) {

        SectionContent content() {
            return new SectionContent(name, fields.stream().map(FieldRequest::content).toList());
        }
    }

    record FieldRequest(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Size(max = 80) String label,
            @NotBlank @Size(max = 20) String fieldType,
            @Size(max = 30) String component,
            boolean required,
            List<String> options,
            Map<String, Object> validation,
            @Size(max = 4) String requiresModule) {

        FieldContent content() {
            return new FieldContent(code, label, fieldType, component, required, options, validation, requiresModule);
        }
    }
}
