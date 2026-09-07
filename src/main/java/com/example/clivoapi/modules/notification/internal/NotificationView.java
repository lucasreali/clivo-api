package com.example.clivoapi.modules.notification.internal;

import com.example.clivoapi.modules.notification.NotificationSnapshot;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

record NotificationView(
        UUID id,
        UUID appointmentId,
        LocalDateTime appointmentStart,
        String channel,
        String recipient,
        String status,
        Instant sentAt,
        String reply) {

    static NotificationView of(NotificationSnapshot reminder) {
        return new NotificationView(
                reminder.id(),
                reminder.appointmentId(),
                reminder.appointmentStart(),
                reminder.channel().name(),
                reminder.recipient().asText(),
                reminder.status().name(),
                reminder.delivery().orElse(null),
                reminder.answer().orElse(null));
    }
}
