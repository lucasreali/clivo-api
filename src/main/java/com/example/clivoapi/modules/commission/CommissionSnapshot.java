package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.money.Money;

public record CommissionSnapshot(
        Long id,
        Long encounterId,
        Long practitionerId,
        String practitionerName,
        CommissionRate percentage,
        Money amount,
        CommissionPeriod period,
        CommissionStatus status) {
}
