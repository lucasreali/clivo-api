package com.example.clivoapi.patterns.chain;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentProposal;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.core.scheduling.ScheduleBlockService;
import com.example.clivoapi.core.scheduling.ScheduleBlockSnapshot;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(ScheduleBlockValidator.ORDER)
public class ScheduleBlockValidator implements AppointmentValidator {

    public static final int ORDER = 20;

    private final ScheduleBlockService blocks;

    ScheduleBlockValidator(ScheduleBlockService blocks) {
        this.blocks = blocks;
    }

    @Override
    public void validate(AppointmentProposal proposal) {
        blocks.firstBlocking(proposal.practitionerId(), proposal.period()).ifPresent(this::refuse);
    }

    private void refuse(ScheduleBlockSnapshot block) {
        throw new BusinessException("the agenda is blocked %s: %s".formatted(block.period(), block.reason()));
    }
}
