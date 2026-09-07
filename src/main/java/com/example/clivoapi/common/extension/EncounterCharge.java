package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.money.Money;
import java.util.Optional;
import java.util.UUID;

public record EncounterCharge(
        UUID encounterId, ChargeState state, Money net, Money paid, Money outstanding, String coverage) {

    private static final String INSURANCE = "INSURANCE";

    public static EncounterCharge none(UUID encounterId) {
        return new EncounterCharge(
                encounterId, ChargeState.NO_CHARGE, Money.zero(), Money.zero(), Money.zero(), null);
    }

    public Optional<String> coveredBy() {
        return Optional.ofNullable(coverage);
    }

    public boolean coveredByInsurance() {
        return INSURANCE.equals(coverage);
    }
}
