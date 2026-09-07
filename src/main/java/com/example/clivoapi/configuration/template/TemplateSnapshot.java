package com.example.clivoapi.configuration.template;

import com.example.clivoapi.common.extension.ModuleCode;
import java.util.UUID;

public record TemplateSnapshot(
        UUID id,
        String name,
        short version,
        RecordTemplateStatus status,
        ModuleCode requiresModule,
        TemplateContent content) {
}
