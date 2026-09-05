package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.modules.commission.PractitionerCommissionSnapshot;
import java.math.BigDecimal;

record RateView(Long id, Long practitionerId, String practitionerName, BigDecimal percentage) {

    static RateView of(PractitionerCommissionSnapshot earner) {
        return new RateView(
                earner.id(), earner.practitionerId(), earner.practitionerName(), earner.rate().value());
    }
}
