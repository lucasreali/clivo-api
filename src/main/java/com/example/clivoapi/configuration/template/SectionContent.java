package com.example.clivoapi.configuration.template;

import java.util.List;

public record SectionContent(String name, List<FieldContent> fields) {

    public SectionContent {
        fields = List.copyOf(fields);
    }
}
