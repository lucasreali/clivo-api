package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.InsurancePlanSnapshot;
import java.math.BigDecimal;

record PlanView(Long id, String name, BigDecimal reimbursementPercentage, String status) {

    static PlanView of(InsurancePlanSnapshot plan) {
        return new PlanView(
                plan.id(),
                plan.details().name(),
                plan.details().reimbursement().value(),
                plan.status().name());
    }
}
