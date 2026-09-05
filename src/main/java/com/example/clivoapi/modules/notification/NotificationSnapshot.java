package com.example.clivoapi.modules.notification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public record NotificationSnapshot(
        Long id,
        Long appointmentId,
        LocalDateTime appointmentStart,
        NotificationChannel channel,
        Recipient recipient,
        NotificationStatus status,
        Instant sentAt,
        String reply) {

    public Optional<Instant> delivery() {
        return Optional.ofNullable(sentAt);
    }

    public Optional<String> answer() {
        return Optional.ofNullable(reply);
    }
}
