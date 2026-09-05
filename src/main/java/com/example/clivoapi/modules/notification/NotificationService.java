package com.example.clivoapi.modules.notification;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.core.scheduling.SchedulingService;
import com.example.clivoapi.modules.notification.internal.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private static final ParameterCode REMINDER_LEAD_HOURS = new ParameterCode("reminder_lead_hours");

    private final NotificationRepository notifications;
    private final SchedulingService appointments;
    private final ClinicParameters parameters;
    private final MessageGateway gateway;

    NotificationService(
            NotificationRepository notifications,
            SchedulingService appointments,
            ClinicParameters parameters,
            MessageGateway gateway) {
        this.notifications = notifications;
        this.appointments = appointments;
        this.parameters = parameters;
        this.gateway = gateway;
    }

    public NotificationSnapshot schedule(Long appointmentId, NotificationChannel channel, Recipient recipient) {
        Notification reminder =
                new Notification(appointments.reference(appointmentId), channel, recipient);
        return notifications.save(reminder).snapshot();
    }

    public List<NotificationSnapshot> dispatchDue() {
        return dueNow().stream().map(this::deliver).toList();
    }

    public NotificationSnapshot registerResponse(Long id, String answer) {
        Notification reminder = notificationOf(id);
        reminder.registerResponse(answer);
        return notifications.save(reminder).snapshot();
    }

    @Transactional(readOnly = true)
    public List<NotificationSnapshot> forAppointment(Long appointmentId) {
        return notifications.findByAppointmentIdOrderByIdAsc(appointmentId).stream()
                .map(Notification::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationSnapshot> pending() {
        return notifications.findByStatusOrderByIdAsc(NotificationStatus.PENDING).stream()
                .map(Notification::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationSnapshot findOne(Long id) {
        return notificationOf(id).snapshot();
    }

    private NotificationSnapshot deliver(Notification reminder) {
        reminder.recordDelivery(gateway.deliver(reminder.reminder()));
        return notifications.save(reminder).snapshot();
    }

    private List<Notification> dueNow() {
        LocalDateTime horizon = LocalDateTime.now().plusHours(leadHours());
        return notifications.findByStatusOrderByIdAsc(NotificationStatus.PENDING).stream()
                .filter(reminder -> reminder.isDueAt(horizon))
                .toList();
    }

    private int leadHours() {
        return parameters.valueOf(REMINDER_LEAD_HOURS).asInteger();
    }

    private Notification notificationOf(Long id) {
        return notifications.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }
}
