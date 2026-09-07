package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.AppointmentParticipants;
import com.example.clivoapi.core.scheduling.AppointmentSnapshot;
import java.time.LocalDateTime;
import java.util.UUID;

record AppointmentView(
        UUID id,
        UUID customerId,
        String customerName,
        UUID practitionerId,
        String practitionerName,
        UUID serviceId,
        String serviceName,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        boolean waiting,
        String reason) {

    static AppointmentView of(AppointmentSnapshot appointment) {
        AppointmentParticipants participants = appointment.participants();
        TimeWindow period = appointment.period();
        return new AppointmentView(
                appointment.id(),
                participants.customerId(),
                participants.customerName(),
                participants.practitionerId(),
                participants.practitionerName(),
                participants.serviceId(),
                participants.serviceName(),
                period.start(),
                period.end(),
                appointment.status().name(),
                appointment.isWaiting(),
                appointment.reason());
    }
}
