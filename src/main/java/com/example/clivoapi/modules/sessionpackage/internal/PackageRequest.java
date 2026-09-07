package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.modules.sessionpackage.PackagePurchase;
import com.example.clivoapi.modules.sessionpackage.SessionCount;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record PackageRequest(
        @NotNull UUID customerId,
        @NotNull UUID serviceId,
        int totalSessions,
        @NotNull BigDecimal price,
        LocalDate expiresOn) {

    PackagePurchase toPurchase() {
        return new PackagePurchase(
                customerId, serviceId, new SessionCount(totalSessions), new Money(price), expiresOn);
    }
}
