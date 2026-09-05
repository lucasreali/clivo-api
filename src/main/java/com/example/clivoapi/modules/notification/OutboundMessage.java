package com.example.clivoapi.modules.notification;

public record OutboundMessage(NotificationChannel channel, Recipient recipient, String text) {
}
