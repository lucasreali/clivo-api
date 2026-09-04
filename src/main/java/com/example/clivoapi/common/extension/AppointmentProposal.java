package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.time.TimeWindow;

public record AppointmentProposal(
        Long appointmentId,
        Long customerId,
        Long practitionerId,
        Long serviceId,
        TimeWindow period) {

    public static AppointmentProposal booking(
            Long customerId, Long practitionerId, Long serviceId, TimeWindow period) {
        return new AppointmentProposal(null, customerId, practitionerId, serviceId, period);
    }

    public static AppointmentProposal rescheduling(
            Long appointmentId, Long customerId, Long practitionerId, Long serviceId, TimeWindow period) {
        return new AppointmentProposal(appointmentId, customerId, practitionerId, serviceId, period);
    }

    public boolean isRescheduling() {
        return appointmentId != null;
    }

    public boolean identifies(Long candidate) {
        return appointmentId != null && appointmentId.equals(candidate);
    }

    public boolean concerns(Long practitioner) {
        return practitionerId.equals(practitioner);
    }
}
