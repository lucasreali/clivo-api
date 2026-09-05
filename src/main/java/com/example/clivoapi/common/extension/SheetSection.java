package com.example.clivoapi.common.extension;

import java.util.List;

public record SheetSection(String name, List<SheetField> fields) {

    public SheetSection {
        fields = List.copyOf(fields);
    }
}
