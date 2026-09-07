package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.money.Money;
import java.util.UUID;

public record CommissionSnapshot(
        UUID id,
        UUID encounterId,
        UUID practitionerId,
        String practitionerName,
        CommissionRate percentage,
        Money amount,
        CommissionPeriod period,
        CommissionStatus status) {
}
