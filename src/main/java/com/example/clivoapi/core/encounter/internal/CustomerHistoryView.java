package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import com.example.clivoapi.core.encounter.CustomerHistory;
import com.example.clivoapi.core.encounter.EncounterCounts;
import com.example.clivoapi.core.encounter.FinancialSummary;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record CustomerHistoryView(
        UUID customerId,
        Instant customerSince,
        List<EncounterHistoryView> encounters,
        CountsView counts,
        SummaryView financials,
        List<StandingAlertView> alerts) {

    static CustomerHistoryView of(CustomerHistory history) {
        return new CustomerHistoryView(
                history.customerId(),
                history.customerSince(),
                history.entries().stream().map(EncounterHistoryView::of).toList(),
                CountsView.of(history.counts()),
                SummaryView.of(history.financials()),
                history.standingAlerts().map(CustomerHistoryView::viewsOf).orElse(null));
    }

    private static List<StandingAlertView> viewsOf(List<ClinicalAlertSnapshot> alerts) {
        return alerts.stream().map(StandingAlertView::of).toList();
    }

    record CountsView(int total, Map<String, Integer> byStatus) {

        static CountsView of(EncounterCounts counts) {
            return new CountsView(counts.total(), namedCountsOf(counts));
        }

        private static Map<String, Integer> namedCountsOf(EncounterCounts counts) {
            return counts.byStatus().entrySet().stream()
                    .collect(toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        }
    }

    record SummaryView(BigDecimal paid, BigDecimal outstanding) {

        static SummaryView of(FinancialSummary financials) {
            return new SummaryView(financials.paid().amount(), financials.outstanding().amount());
        }
    }

}
