package com.example.clivoapi.modules.notification.internal;

import com.example.clivoapi.modules.notification.NotificationChannel;
import com.example.clivoapi.modules.notification.Recipient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record NotificationRequest(@NotNull Long appointmentId, @NotBlank String channel, String recipient) {

    NotificationChannel toChannel() {
        return NotificationChannel.of(channel);
    }

    Recipient toRecipient() {
        return new Recipient(recipient);
    }
}
