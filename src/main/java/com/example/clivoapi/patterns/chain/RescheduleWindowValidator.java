package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.core.scheduling.AppointmentBook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(RescheduleWindowValidator.ORDER)
public class RescheduleWindowValidator implements AppointmentValidator {

    public static final int ORDER = 40;

    private static final ParameterCode RESCHEDULE_WINDOW = new ParameterCode("reschedule_window_hours");

    private final AppointmentBook book;
    private final ClinicParameters parameters;

    RescheduleWindowValidator(AppointmentBook book, ClinicParameters parameters) {
        this.book = book;
        this.parameters = parameters;
    }

    @Override
    public void validate(AppointmentProposal proposal) {
        if (!proposal.isRescheduling()) {
            return;
        }
        int windowHours = parameters.valueOf(RESCHEDULE_WINDOW).asInteger();
        if (book.reference(proposal.appointmentId()).canBeRescheduled(windowHours)) {
            return;
        }
        throw new BusinessException(
                "an appointment can only be rescheduled until %d hours before it starts".formatted(windowHours));
    }
}
