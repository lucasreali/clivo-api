package com.example.clivoapi.common.extension;

import java.util.List;

public record SheetField(
        String code, String label, String fieldType, boolean required, List<String> options, Object value) {

    public SheetField {
        options = List.copyOf(options);
    }
}
