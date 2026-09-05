package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.CoveragePercentage;
import com.example.clivoapi.modules.insurance.PlanDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record PlanRequest(@NotBlank String name, @NotNull BigDecimal reimbursementPercentage) {

    PlanDetails toDetails() {
        return new PlanDetails(name, new CoveragePercentage(reimbursementPercentage));
    }
}
