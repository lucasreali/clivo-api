package com.example.clivoapi.common.extension;

public record ModuleCode(String value) {

    public ModuleCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a module code is required");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
