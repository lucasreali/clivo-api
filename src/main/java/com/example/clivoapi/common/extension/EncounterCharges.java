package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.money.Money;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record EncounterCharges(Map<UUID, EncounterCharge> byEncounter) {

    public EncounterCharges {
        byEncounter = Map.copyOf(byEncounter);
    }

    public static EncounterCharges none() {
        return new EncounterCharges(Map.of());
    }

    public EncounterCharge of(UUID encounterId) {
        return byEncounter.getOrDefault(encounterId, EncounterCharge.none(encounterId));
    }

    public Money paidTotal() {
        return sumOf(EncounterCharge::paid);
    }

    public Money outstandingTotal() {
        return sumOf(EncounterCharge::outstanding);
    }

    private Money sumOf(Function<EncounterCharge, Money> part) {
        return byEncounter.values().stream().map(part).reduce(Money.zero(), Money::plus);
    }
}
