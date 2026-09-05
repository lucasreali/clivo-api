package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.exception.BusinessException;

public record PlanDetails(String name, CoveragePercentage reimbursement) {

    public PlanDetails {
        name = named(name);
    }

    private static String named(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("an insurance plan needs a name");
        }
        return name.trim();
    }
}
