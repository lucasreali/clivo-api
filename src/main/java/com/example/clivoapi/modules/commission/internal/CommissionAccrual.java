package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.commission.CommissionService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CommissionAccrual.AFTER_COVERAGE)
class CommissionAccrual implements EncounterCompletionListener {

    static final int AFTER_COVERAGE = 200;

    private static final ModuleCode COMMISSION = new ModuleCode("commission");

    private final ModuleActivationState activation;
    private final CommissionService commissions;

    CommissionAccrual(ModuleActivationState activation, CommissionService commissions) {
        this.activation = activation;
        this.commissions = commissions;
    }

    @Override
    public void onCompleted(CompletedEncounter encounter) {
        if (activation.isActive(COMMISSION)) {
            commissions.accrueFor(encounter);
        }
    }
}
