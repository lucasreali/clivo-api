package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.EncounterCharges;
import com.example.clivoapi.common.money.Money;

public record FinancialSummary(Money paid, Money outstanding) {

    public static FinancialSummary of(EncounterCharges charges) {
        return new FinancialSummary(charges.paidTotal(), charges.outstandingTotal());
    }
}
