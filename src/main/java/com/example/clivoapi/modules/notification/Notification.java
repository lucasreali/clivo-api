package com.example.clivoapi.modules.notification;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.scheduling.Appointment;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "notification")
public class Notification extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, updatable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NotificationChannel channel;

    @Embedded
    private Recipient recipient;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private String reply;

    @Column(name = "replied_at")
    private Instant repliedAt;

    protected Notification() {
    }

    public Notification(Appointment appointment, NotificationChannel channel, Recipient recipient) {
        this.appointment = appointment;
        this.channel = channel;
        this.recipient = recipient;
        this.status = NotificationStatus.PENDING;
    }

    public UUID id() {
        return id;
    }

    public boolean isPending() {
        return status.awaitsDelivery();
    }

    public boolean isDueAt(LocalDateTime moment) {
        return !appointmentStart().isAfter(moment);
    }

    public OutboundMessage reminder() {
        return new OutboundMessage(channel, recipient, reminderText());
    }

    public void markAsSent() {
        requirePending("sent");
        status = NotificationStatus.SENT;
        sentAt = Instant.now();
    }

    public void markAsFailed() {
        requirePending("failed");
        status = NotificationStatus.FAILED;
    }

    public void recordDelivery(boolean delivered) {
        if (delivered) {
            markAsSent();
            return;
        }
        markAsFailed();
    }

    public void registerResponse(String answer) {
        requireDelivered();
        reply = new Reply(answer).asText();
        repliedAt = Instant.now();
        status = NotificationStatus.REPLIED;
    }

    public NotificationSnapshot snapshot() {
        return new NotificationSnapshot(
                id, appointment.id(), appointmentStart(), channel, recipient, status, sentAt, reply);
    }

    private LocalDateTime appointmentStart() {
        return appointment.period().start();
    }

    private String reminderText() {
        return "reminder: your appointment starts at %s".formatted(appointmentStart());
    }

    private void requirePending(String operation) {
        if (isPending()) {
            return;
        }
        throw new BusinessException("a reminder in status %s cannot be marked as %s".formatted(status, operation));
    }

    private void requireDelivered() {
        if (status.wasDelivered()) {
            return;
        }
        throw new BusinessException("a reminder in status %s received no answer to register".formatted(status));
    }
}
