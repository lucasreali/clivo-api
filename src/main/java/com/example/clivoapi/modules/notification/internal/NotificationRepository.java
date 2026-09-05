package com.example.clivoapi.modules.notification.internal;

import com.example.clivoapi.modules.notification.Notification;
import com.example.clivoapi.modules.notification.NotificationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByAppointmentIdOrderByIdAsc(Long appointmentId);

    List<Notification> findByStatusOrderByIdAsc(NotificationStatus status);
}
