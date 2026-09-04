package com.example.clivoapi.configuration.template;

import java.util.List;
import java.util.Map;

public record FieldContent(
        String code,
        String label,
        String fieldType,
        String component,
        boolean required,
        List<String> options,
        Map<String, Object> validation,
        String requiresModule) {
}
