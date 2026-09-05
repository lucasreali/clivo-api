package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import java.time.LocalDate;

public record PackagePurchase(
        Long customerId, Long serviceId, SessionCount totalSessions, Money price, LocalDate expiresOn) {

    public PackagePurchase {
        expiresOn = ahead(expiresOn);
    }

    private static LocalDate ahead(LocalDate expiresOn) {
        if (expiresOn == null) {
            throw new BusinessException("a package is sold with an expiry date");
        }
        if (expiresOn.isBefore(LocalDate.now())) {
            throw new BusinessException("a package cannot be sold already expired");
        }
        return expiresOn;
    }
}
