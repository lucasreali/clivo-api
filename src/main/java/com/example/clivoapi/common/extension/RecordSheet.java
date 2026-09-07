package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.UUID;

public record RecordSheet(UUID templateId, String templateName, int templateVersion, List<SheetSection> sections) {

    public RecordSheet {
        sections = List.copyOf(sections);
    }
}
