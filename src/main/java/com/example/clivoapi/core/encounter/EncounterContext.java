package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CoverageDirectory;
import com.example.clivoapi.core.clinical.ClinicalAlertService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class EncounterContext {

    private final ClinicalAlertService alerts;
    private final CoverageDirectories coverage;

    EncounterContext(ClinicalAlertService alerts, List<CoverageDirectory> directories) {
        this.alerts = alerts;
        this.coverage = new CoverageDirectories(directories);
    }

    ClinicalContext of(UUID customerId) {
        return new ClinicalContext(coverage.coverageOf(customerId).orElse(null), alerts.of(customerId));
    }

    ClinicalContext coverageOnlyOf(UUID customerId) {
        return new ClinicalContext(coverage.coverageOf(customerId).orElse(null), null);
    }
}
