package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.money.Money;
import java.time.LocalDate;

public record SessionPackageSnapshot(
        Long id,
        Long customerId,
        Long serviceId,
        String serviceName,
        int totalSessions,
        int usedSessions,
        int remainingSessions,
        Money price,
        LocalDate expiresOn,
        SessionPackageStatus status,
        boolean active) {
}
