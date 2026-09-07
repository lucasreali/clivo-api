package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SchedulingService {

    private final AppointmentBook book;
    private final AppointmentAssembler assembler;
    private final AppointmentValidation validation;

    SchedulingService(AppointmentBook book, AppointmentAssembler assembler, List<AppointmentValidator> validators) {
        this.book = book;
        this.assembler = assembler;
        this.validation = new AppointmentValidation(validators);
    }

    public AppointmentSnapshot schedule(AppointmentBooking booking) {
        Appointment appointment = assembler.assemble(booking);
        validation.check(appointment.proposal());
        return book.record(appointment).snapshot();
    }

    public AppointmentSnapshot reschedule(UUID id, LocalDateTime start) {
        Appointment appointment = book.reference(id);
        AppointmentProposal proposal = appointment.proposalToStartAt(start);
        validation.check(proposal);
        appointment.rescheduleTo(proposal.period());
        return book.record(appointment).snapshot();
    }

    public AppointmentSnapshot cancel(UUID id, CancellationReason reason) {
        Appointment appointment = book.reference(id);
        appointment.cancel(reason);
        return book.record(appointment).snapshot();
    }

    public AppointmentSnapshot checkIn(UUID id) {
        Appointment appointment = book.reference(id);
        appointment.checkIn();
        return book.record(appointment).snapshot();
    }

    public AppointmentSnapshot markNoShow(UUID id, CancellationReason reason) {
        Appointment appointment = book.reference(id);
        appointment.markNoShow(reason);
        return book.record(appointment).snapshot();
    }

    @Transactional(readOnly = true)
    public List<AppointmentSnapshot> dayPanel(LocalDate day) {
        return book.onDay(day).stream().map(Appointment::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public Appointment reference(UUID id) {
        return book.reference(id);
    }

    @Transactional(readOnly = true)
    public AppointmentSnapshot findOne(UUID id) {
        return book.reference(id).snapshot();
    }
}
