package com.example.clivoapi.modules.notification;

public enum NotificationStatus {

    PENDING,
    SENT,
    FAILED,
    REPLIED;

    public boolean awaitsDelivery() {
        return this == PENDING;
    }

    public boolean wasDelivered() {
        return this == SENT || this == REPLIED;
    }
}
