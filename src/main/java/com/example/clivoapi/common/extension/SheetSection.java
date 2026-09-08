package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.function.UnaryOperator;

public record SheetSection(String name, List<SheetField> fields) {

    public SheetSection {
        fields = List.copyOf(fields);
    }

    public SheetSection withFieldsResolved(UnaryOperator<SheetField> resolution) {
        return new SheetSection(name, fields.stream().map(resolution).toList());
    }
}
