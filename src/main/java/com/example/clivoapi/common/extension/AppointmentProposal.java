package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.time.TimeWindow;
import java.util.UUID;

public record AppointmentProposal(
        UUID appointmentId,
        UUID customerId,
        UUID practitionerId,
        UUID serviceId,
        TimeWindow period) {

    public static AppointmentProposal booking(
            UUID customerId, UUID practitionerId, UUID serviceId, TimeWindow period) {
        return new AppointmentProposal(null, customerId, practitionerId, serviceId, period);
    }

    public static AppointmentProposal rescheduling(
            UUID appointmentId, UUID customerId, UUID practitionerId, UUID serviceId, TimeWindow period) {
        return new AppointmentProposal(appointmentId, customerId, practitionerId, serviceId, period);
    }

    public boolean isRescheduling() {
        return appointmentId != null;
    }

    public boolean identifies(UUID candidate) {
        return appointmentId != null && appointmentId.equals(candidate);
    }

    public boolean concerns(UUID practitioner) {
        return practitionerId.equals(practitioner);
    }
}
