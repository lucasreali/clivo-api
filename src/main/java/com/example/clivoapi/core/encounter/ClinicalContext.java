package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.CoverageNote;
import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.util.List;
import java.util.Optional;

public record ClinicalContext(CoverageNote coverage, List<ClinicalAlertSnapshot> alerts) {

    public static ClinicalContext undisclosed() {
        return new ClinicalContext(null, null);
    }

    public Optional<CoverageNote> covering() {
        return Optional.ofNullable(coverage);
    }

    public Optional<List<ClinicalAlertSnapshot>> standingAlerts() {
        return Optional.ofNullable(alerts);
    }
}
