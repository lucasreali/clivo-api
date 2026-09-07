package com.example.clivoapi.core.encounter;

import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CustomerHistory(
        UUID customerId,
        Instant customerSince,
        List<HistoryEntry> entries,
        EncounterCounts counts,
        FinancialSummary financials,
        List<ClinicalAlertSnapshot> alerts) {

    public CustomerHistory {
        entries = List.copyOf(entries);
    }

    public Optional<List<ClinicalAlertSnapshot>> standingAlerts() {
        return Optional.ofNullable(alerts);
    }
}
