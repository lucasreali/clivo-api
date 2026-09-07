package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import java.time.LocalDate;
import java.util.UUID;

public record PackagePurchase(
        UUID customerId, UUID serviceId, SessionCount totalSessions, Money price, LocalDate expiresOn) {

    public PackagePurchase {
        expiresOn = ahead(expiresOn);
    }

    private static LocalDate ahead(LocalDate expiresOn) {
        if (expiresOn == null) {
            throw new BusinessException("a package is sold with an expiry date");
        }
        if (!expiresOn.isAfter(LocalDate.now())) {
            throw new BusinessException("expiresOn: a package cannot be sold already expired");
        }
        return expiresOn;
    }
}
