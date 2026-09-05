package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.exception.BusinessException;

public record SessionCount(int value) {

    private static final int MAXIMUM = 200;

    public SessionCount {
        value = withinRange(value);
    }

    public short asShort() {
        return (short) value;
    }

    private static int withinRange(int value) {
        if (value <= 0) {
            throw new BusinessException("a package is sold with at least one session");
        }
        if (value > MAXIMUM) {
            throw new BusinessException("a package holds at most %d sessions".formatted(MAXIMUM));
        }
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
