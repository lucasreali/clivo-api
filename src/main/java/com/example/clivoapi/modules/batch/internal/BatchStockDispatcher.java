package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.BatchDispatcher;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.batch.BatchService;
import com.example.clivoapi.modules.inventory.Quantity;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
class BatchStockDispatcher implements BatchDispatcher {

    private static final ModuleCode BATCH = new ModuleCode("batch");

    private final ModuleActivationState activation;
    private final BatchService batches;

    /**
     * The batch service reaches the inventory to read a product, and the inventory
     * reaches this dispatcher to spend a lot; resolving the service on use rather
     * than on construction is what keeps the two from forming a bean cycle.
     */
    BatchStockDispatcher(ModuleActivationState activation, @Lazy BatchService batches) {
        this.activation = activation;
        this.batches = batches;
    }

    @Override
    public Optional<BatchChoice> dispatch(UUID productId, BigDecimal quantity) {
        if (!activation.isActive(BATCH)) {
            return Optional.empty();
        }
        return batches.spendFor(productId, new Quantity(quantity));
    }
}
