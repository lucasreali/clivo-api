package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import java.util.List;

public class AppointmentValidation {

    private final List<AppointmentValidator> validators;

    public AppointmentValidation(List<AppointmentValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    public void check(AppointmentProposal proposal) {
        validators.forEach(validator -> validator.validate(proposal));
    }
}
