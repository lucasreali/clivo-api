package com.example.clivoapi.modules.batch;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.text.TextField;
import com.example.clivoapi.modules.inventory.Quantity;
import java.time.LocalDate;
import java.util.Optional;

public record BatchDetails(BatchCode code, LocalDate expiresOn, Quantity quantity, String manufacturer) {

    private static final TextField MANUFACTURER = new TextField("a manufacturer", 80);

    public BatchDetails {
        expiresOn = dated(expiresOn);
        quantity.requirePositive("a batch quantity");
        manufacturer = MANUFACTURER.optional(manufacturer);
    }

    public Optional<String> madeBy() {
        return Optional.ofNullable(manufacturer);
    }

    private static LocalDate dated(LocalDate expiresOn) {
        if (expiresOn == null) {
            throw new BusinessException("expiresOn: a batch carries the expiry date printed on its package");
        }
        return expiresOn;
    }
}
