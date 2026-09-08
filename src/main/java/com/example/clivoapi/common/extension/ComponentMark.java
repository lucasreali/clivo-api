package com.example.clivoapi.common.extension;

public record ComponentMark(String code, String label, String rendering, MarkTarget appliesTo) {

    public boolean accepts(RegionMarking marking) {
        return appliesTo.accepts(marking);
    }
}
