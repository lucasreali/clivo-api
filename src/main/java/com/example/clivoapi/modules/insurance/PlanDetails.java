package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.text.TextField;

public record PlanDetails(String name, CoveragePercentage reimbursement) {

    private static final TextField NAME = new TextField("an insurance plan name", 120);

    public PlanDetails {
        name = NAME.required(name);
        reimbursement = declared(reimbursement);
    }

    private static CoveragePercentage declared(CoveragePercentage reimbursement) {
        if (reimbursement != null) {
            return reimbursement;
        }
        throw new BusinessException("reimbursementPercentage is required");
    }
}
