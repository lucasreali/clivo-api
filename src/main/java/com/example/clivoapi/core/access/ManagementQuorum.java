package com.example.clivoapi.core.access;

public record ManagementQuorum(long activeManagers) {

    public ManagementQuorum {
        if (activeManagers < 0) {
            throw new IllegalArgumentException("a clinic never has a negative number of managers");
        }
    }

    public boolean hasAnotherManager() {
        return activeManagers > 1;
    }
}
