package com.example.clivoapi.common.extension;

public enum ChargeState {

    NO_CHARGE,
    COVERED,
    PAID,
    OUTSTANDING;

    public boolean isSettled() {
        return this == PAID || this == COVERED;
    }
}
