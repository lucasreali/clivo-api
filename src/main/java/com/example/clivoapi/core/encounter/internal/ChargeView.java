package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.EncounterCharge;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
record ChargeView(String state, BigDecimal net, BigDecimal paid, BigDecimal outstanding, String coverage) {

    static ChargeView of(EncounterCharge charge) {
        return new ChargeView(
                charge.state().name(),
                charge.net().amount(),
                charge.paid().amount(),
                charge.outstanding().amount(),
                charge.coveredBy().orElse(null));
    }
}
