package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.PractitionerCommissionSnapshot;
import java.math.BigDecimal;
import java.util.UUID;

record RateView(UUID id, UUID practitionerId, String practitionerName, BigDecimal percentage) {

    static RateView of(PractitionerCommissionSnapshot earner) {
        return new RateView(
                earner.id(), earner.practitionerId(), earner.practitionerName(), earner.rate().value());
    }
}
