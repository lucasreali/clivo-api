package com.example.clivoapi.core.encounter;

public enum EncounterStatus {

    DRAFT,
    COMPLETED,
    CANCELLED;

    public boolean isOpen() {
        return this == DRAFT;
    }
}
