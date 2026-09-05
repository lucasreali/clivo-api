package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Optional;

public record EncounterOpening(
        Long appointmentId, Long customerId, Long practitionerId, Long serviceId, Long recordTemplateId) {

    public EncounterOpening {
        require(recordTemplateId, "record template");
    }

    public static EncounterOpening forAppointment(Long appointmentId, Long recordTemplateId) {
        require(appointmentId, "appointment");
        return new EncounterOpening(appointmentId, null, null, null, recordTemplateId);
    }

    public static EncounterOpening walkIn(
            Long customerId, Long practitionerId, Long serviceId, Long recordTemplateId) {
        require(customerId, "customer");
        require(practitionerId, "practitioner");
        require(serviceId, "service");
        return new EncounterOpening(null, customerId, practitionerId, serviceId, recordTemplateId);
    }

    public Optional<Long> appointment() {
        return Optional.ofNullable(appointmentId);
    }

    private static void require(Long id, String party) {
        if (id != null) {
            return;
        }
        throw new BusinessException("an encounter needs a %s".formatted(party));
    }
}
