package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.CommissionRate;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record RateRequest(@NotNull BigDecimal percentage) {

    CommissionRate toRate() {
        return new CommissionRate(percentage);
    }
}
