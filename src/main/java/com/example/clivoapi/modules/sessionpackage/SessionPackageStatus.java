package com.example.clivoapi.modules.sessionpackage;

public enum SessionPackageStatus {

    ACTIVE,
    EXHAUSTED,
    EXPIRED,
    CANCELLED;

    public boolean isOpen() {
        return this == ACTIVE;
    }
}
