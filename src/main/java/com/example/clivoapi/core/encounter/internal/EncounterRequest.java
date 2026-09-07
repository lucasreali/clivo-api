package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.EncounterOpening;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

record EncounterRequest(
        UUID appointmentId, UUID customerId, UUID practitionerId, UUID serviceId, @NotNull UUID recordTemplateId) {

    EncounterOpening toOpening() {
        return Optional.ofNullable(appointmentId)
                .map(id -> EncounterOpening.forAppointment(id, recordTemplateId))
                .orElseGet(() -> EncounterOpening.walkIn(customerId, practitionerId, serviceId, recordTemplateId));
    }
}
