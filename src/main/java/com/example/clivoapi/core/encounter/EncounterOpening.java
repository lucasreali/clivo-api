package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;

public record EncounterOpening(
        UUID appointmentId, UUID customerId, UUID practitionerId, UUID serviceId, UUID recordTemplateId) {

    public EncounterOpening {
        require(recordTemplateId, "record template");
    }

    public static EncounterOpening forAppointment(UUID appointmentId, UUID recordTemplateId) {
        require(appointmentId, "appointment");
        return new EncounterOpening(appointmentId, null, null, null, recordTemplateId);
    }

    public static EncounterOpening walkIn(
            UUID customerId, UUID practitionerId, UUID serviceId, UUID recordTemplateId) {
        require(customerId, "customer");
        require(practitionerId, "practitioner");
        require(serviceId, "service");
        return new EncounterOpening(null, customerId, practitionerId, serviceId, recordTemplateId);
    }

    public Optional<UUID> appointment() {
        return Optional.ofNullable(appointmentId);
    }

    private static void require(UUID id, String party) {
        if (id != null) {
            return;
        }
        throw new BusinessException("an encounter needs a %s".formatted(party));
    }
}
