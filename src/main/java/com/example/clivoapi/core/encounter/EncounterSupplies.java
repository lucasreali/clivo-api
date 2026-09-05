package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.StockDispenser;
import com.example.clivoapi.common.extension.SuppliesUsed;
import java.util.List;

class EncounterSupplies {

    private final List<StockDispenser> dispensers;

    EncounterSupplies(List<StockDispenser> dispensers) {
        this.dispensers = List.copyOf(dispensers);
    }

    void dispense(SuppliesUsed supplies) {
        dispensers.forEach(dispenser -> dispenser.dispense(supplies));
    }
}
