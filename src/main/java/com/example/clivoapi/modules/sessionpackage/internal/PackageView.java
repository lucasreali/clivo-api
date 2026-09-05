package com.example.clivoapi.modules.sessionpackage.internal;

import com.example.clivoapi.modules.sessionpackage.SessionPackageSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;

record PackageView(
        Long id,
        Long customerId,
        Long serviceId,
        String serviceName,
        int totalSessions,
        int usedSessions,
        int remainingSessions,
        BigDecimal price,
        LocalDate expiresOn,
        String status,
        boolean active) {

    static PackageView of(SessionPackageSnapshot sold) {
        return new PackageView(
                sold.id(),
                sold.customerId(),
                sold.serviceId(),
                sold.serviceName(),
                sold.totalSessions(),
                sold.usedSessions(),
                sold.remainingSessions(),
                sold.price().amount(),
                sold.expiresOn(),
                sold.status().name(),
                sold.active());
    }
}
