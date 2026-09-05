package com.example.clivoapi.common.extension;

import java.util.List;

public record RecordSheet(Long templateId, String templateName, int templateVersion, List<SheetSection> sections) {

    public RecordSheet {
        sections = List.copyOf(sections);
    }
}
