package com.example.clivoapi.common.extension;

public record RecordFilling(Long templateId, RecordValues values) {

    public static RecordFilling blank(Long templateId) {
        return new RecordFilling(templateId, RecordValues.empty());
    }
}
