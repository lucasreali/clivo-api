package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.internal.AppointmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AppointmentBook {

    private final AppointmentRepository appointments;

    AppointmentBook(AppointmentRepository appointments) {
        this.appointments = appointments;
    }

    public Appointment reference(UUID id) {
        return appointments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    @Transactional
    public Appointment record(Appointment appointment) {
        return appointments.save(appointment);
    }

    public Optional<Appointment> firstColliding(AppointmentProposal proposal) {
        return around(proposal.period(), proposal.practitionerId()).stream()
                .filter(booked -> booked.collidesWith(proposal))
                .findFirst();
    }

    public List<Appointment> onDay(LocalDate day) {
        TimeWindow window = TimeWindow.wholeDay(day);
        return appointments.findByPeriodStartsAtGreaterThanEqualAndPeriodStartsAtLessThanOrderByPeriodStartsAtAsc(
                window.startsAt(), window.endsAt());
    }

    private List<Appointment> around(TimeWindow period, UUID practitionerId) {
        return appointments.findByPractitionerIdAndPeriodStartsAtLessThanAndPeriodEndsAtGreaterThan(
                practitionerId, period.endsAt(), period.startsAt());
    }
}
