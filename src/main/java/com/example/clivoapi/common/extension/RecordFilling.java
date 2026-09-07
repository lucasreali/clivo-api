package com.example.clivoapi.common.extension;

import java.util.UUID;

public record RecordFilling(UUID templateId, RecordValues values) {

    public static RecordFilling blank(UUID templateId) {
        return new RecordFilling(templateId, RecordValues.empty());
    }
}
