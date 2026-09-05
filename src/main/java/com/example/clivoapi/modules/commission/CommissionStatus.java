package com.example.clivoapi.modules.commission;

public enum CommissionStatus {

    OPEN,
    CLOSED;

    public boolean isOpen() {
        return this == OPEN;
    }
}
