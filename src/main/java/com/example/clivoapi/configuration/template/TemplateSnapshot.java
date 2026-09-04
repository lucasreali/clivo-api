package com.example.clivoapi.configuration.template;

import com.example.clivoapi.common.extension.ModuleCode;

public record TemplateSnapshot(
        Long id,
        String name,
        short version,
        RecordTemplateStatus status,
        ModuleCode requiresModule,
        TemplateContent content) {
}
