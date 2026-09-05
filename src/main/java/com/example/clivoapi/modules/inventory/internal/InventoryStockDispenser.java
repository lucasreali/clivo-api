package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.StockDispenser;
import com.example.clivoapi.common.extension.SuppliesUsed;
import com.example.clivoapi.modules.inventory.InventoryService;
import org.springframework.stereotype.Component;

@Component
class InventoryStockDispenser implements StockDispenser {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private final ModuleActivationState activation;
    private final InventoryService inventory;

    InventoryStockDispenser(ModuleActivationState activation, InventoryService inventory) {
        this.activation = activation;
        this.inventory = inventory;
    }

    @Override
    public void dispense(SuppliesUsed supplies) {
        if (activation.isActive(INVENTORY)) {
            inventory.dispense(supplies);
        }
    }
}
