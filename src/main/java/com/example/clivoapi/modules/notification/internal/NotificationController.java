package com.example.clivoapi.modules.notification.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.notification.NotificationService;
import com.example.clivoapi.modules.notification.NotificationSnapshot;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiresModule("notification")
class NotificationController {

    private final NotificationService notifications;

    NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    NotificationView schedule(@Valid @RequestBody NotificationRequest request) {
        return NotificationView.of(notifications.schedule(
                request.appointmentId(), request.toChannel(), request.toRecipient()));
    }

    @GetMapping
    List<NotificationView> forAppointment(@RequestParam Long appointmentId) {
        return viewsOf(notifications.forAppointment(appointmentId));
    }

    @GetMapping("/pending")
    List<NotificationView> pending() {
        return viewsOf(notifications.pending());
    }

    @PostMapping("/dispatch")
    List<NotificationView> dispatchDue() {
        return viewsOf(notifications.dispatchDue());
    }

    @PostMapping("/{id}/reply")
    NotificationView registerResponse(@PathVariable Long id, @RequestBody ReplyRequest request) {
        return NotificationView.of(notifications.registerResponse(id, request.reply()));
    }

    private List<NotificationView> viewsOf(List<NotificationSnapshot> found) {
        return found.stream().map(NotificationView::of).toList();
    }
}
