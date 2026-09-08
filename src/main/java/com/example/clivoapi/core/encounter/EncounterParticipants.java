package com.example.clivoapi.core.encounter;

import java.util.Optional;
import java.util.UUID;

public record EncounterParticipants(
        UUID appointmentId,
        AttendedCustomer customer,
        AttendingPractitioner practitioner,
        ProvidedService service) {

    public Optional<UUID> booking() {
        return Optional.ofNullable(appointmentId);
    }
}
