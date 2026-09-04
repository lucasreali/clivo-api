package com.example.clivoapi.configuration.template;

import java.util.List;

public record TemplateContent(List<SectionContent> sections) {

    public TemplateContent {
        sections = List.copyOf(sections);
    }
}
