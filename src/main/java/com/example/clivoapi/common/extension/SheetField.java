package com.example.clivoapi.common.extension;

import java.util.List;

public record SheetField(
        String code,
        String label,
        String fieldType,
        boolean required,
        List<String> options,
        Object value,
        ComponentDescriptor descriptor,
        List<MarkedRegionState> markings) {

    public SheetField {
        options = List.copyOf(options);
    }

    public boolean describesRegions() {
        return descriptor != null;
    }

    public SheetField resolvedWith(List<MarkedRegionState> resolved) {
        return new SheetField(code, label, fieldType, required, options, value, descriptor, resolved);
    }
}
