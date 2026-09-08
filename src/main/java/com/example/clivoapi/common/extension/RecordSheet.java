package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public record RecordSheet(UUID templateId, String templateName, int templateVersion, List<SheetSection> sections) {

    public RecordSheet {
        sections = List.copyOf(sections);
    }

    public List<SheetField> regionFields() {
        return sections.stream()
                .flatMap(section -> section.fields().stream())
                .filter(SheetField::describesRegions)
                .toList();
    }

    public RecordSheet withFieldsResolved(UnaryOperator<SheetField> resolution) {
        return new RecordSheet(
                templateId,
                templateName,
                templateVersion,
                sections.stream().map(section -> section.withFieldsResolved(resolution)).toList());
    }
}
