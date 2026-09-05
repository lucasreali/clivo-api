package com.example.clivoapi.modules.notification;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Arrays;

public enum NotificationChannel {

    SMS,
    WHATSAPP,
    EMAIL;

    public static NotificationChannel of(String value) {
        return Arrays.stream(values())
                .filter(channel -> channel.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "a reminder goes out through one of %s".formatted(Arrays.toString(values()))));
    }
}
