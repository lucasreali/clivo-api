package com.example.clivoapi.common.extension;

import java.util.List;
import java.util.UUID;

public interface ChargeLedger {

    EncounterCharges chargesOf(List<UUID> encounterIds);
}
