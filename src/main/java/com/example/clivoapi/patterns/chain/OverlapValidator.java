package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.core.scheduling.Appointment;
import com.example.clivoapi.core.scheduling.AppointmentBook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(OverlapValidator.ORDER)
public class OverlapValidator implements AppointmentValidator {

    public static final int ORDER = 30;

    private final AppointmentBook book;

    OverlapValidator(AppointmentBook book) {
        this.book = book;
    }

    @Override
    public void validate(AppointmentProposal proposal) {
        book.firstColliding(proposal).ifPresent(this::refuse);
    }

    private void refuse(Appointment booked) {
        throw new BusinessException(
                "the practitioner already has an appointment from %s".formatted(booked.period()));
    }
}
