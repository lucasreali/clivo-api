package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.EncounterOpening;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

record EncounterRequest(Long appointmentId, Long customerId, Long practitionerId, @NotNull Long recordTemplateId) {

    EncounterOpening toOpening() {
        return Optional.ofNullable(appointmentId)
                .map(id -> EncounterOpening.forAppointment(id, recordTemplateId))
                .orElseGet(() -> EncounterOpening.walkIn(customerId, practitionerId, recordTemplateId));
    }
}
