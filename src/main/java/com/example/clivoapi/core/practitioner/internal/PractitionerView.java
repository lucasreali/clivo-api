package com.example.clivoapi.core.practitioner.internal;

import com.example.clivoapi.core.practitioner.PractitionerSnapshot;
import java.util.List;

record PractitionerView(Long id, String name, String licenseNumber, String status, List<PeriodView> availability) {

    static PractitionerView of(PractitionerSnapshot practitioner) {
        return new PractitionerView(
                practitioner.id(),
                practitioner.details().name(),
                practitioner.details().license().orElse(null),
                practitioner.status().name(),
                practitioner.schedule().periods().stream().map(PeriodView::of).toList());
    }
}
