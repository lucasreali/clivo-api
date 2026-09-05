package com.example.clivoapi.modules.batch;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.modules.inventory.Quantity;
import java.time.LocalDate;
import java.util.Optional;

public record BatchDetails(BatchCode code, LocalDate expiresOn, Quantity quantity, String manufacturer) {

    public BatchDetails {
        expiresOn = dated(expiresOn);
        quantity.requirePositive("a batch quantity");
    }

    public Optional<String> madeBy() {
        return Optional.ofNullable(manufacturer);
    }

    private static LocalDate dated(LocalDate expiresOn) {
        if (expiresOn == null) {
            throw new BusinessException("a batch carries the expiry date printed on its package");
        }
        return expiresOn;
    }
}
