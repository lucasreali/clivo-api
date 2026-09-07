package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.money.Money;
import java.time.LocalDate;
import java.util.UUID;

public record SessionPackageSnapshot(
        UUID id,
        UUID customerId,
        UUID serviceId,
        String serviceName,
        int totalSessions,
        int usedSessions,
        int remainingSessions,
        Money price,
        LocalDate expiresOn,
        SessionPackageStatus status,
        boolean active) {
}
