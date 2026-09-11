package com.example.clivoapi.common.extension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Chooses which batch a dispense takes from and debits it. A clinic without the
 * batch module registers no implementation, so the list the core injects is
 * empty and stock leaves the shelf without belonging to any lot.
 */
public interface BatchDispatcher {

    Optional<BatchChoice> dispatch(UUID productId, BigDecimal quantity);
}
