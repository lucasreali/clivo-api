package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.core.practitioner.Practitioner;
import com.example.clivoapi.core.practitioner.PractitionerService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(AvailabilityValidator.ORDER)
public class AvailabilityValidator implements AppointmentValidator {

    public static final int ORDER = 10;

    private final PractitionerService practitioners;

    AvailabilityValidator(PractitionerService practitioners) {
        this.practitioners = practitioners;
    }

    @Override
    public void validate(AppointmentProposal proposal) {
        Practitioner practitioner = practitioners.reference(proposal.practitionerId());
        if (practitioner.worksThroughout(proposal.period())) {
            return;
        }
        throw new BusinessException(
                "practitioner %s does not attend from %s".formatted(practitioner.name(), proposal.period()));
    }
}
