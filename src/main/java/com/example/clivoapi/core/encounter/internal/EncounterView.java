package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import com.example.clivoapi.core.encounter.AttendedCustomer;
import com.example.clivoapi.core.encounter.AttendingPractitioner;
import com.example.clivoapi.core.encounter.ClinicalContext;
import com.example.clivoapi.core.encounter.EncounterParticipants;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.example.clivoapi.core.encounter.EncounterTiming;
import com.example.clivoapi.core.encounter.ProvidedService;
import com.example.clivoapi.core.encounter.Signature;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record EncounterView(
        UUID id,
        UUID appointmentId,
        String status,
        AttendedCustomerView customer,
        AttendingPractitionerView practitioner,
        ProvidedServiceView service,
        Instant startedAt,
        Instant lastSavedAt,
        Instant completedAt,
        SignatureView signedBy,
        RecordSheet sheet) {

    static EncounterView of(EncounterSnapshot encounter) {
        EncounterParticipants participants = encounter.participants();
        EncounterTiming timing = encounter.timing();
        return new EncounterView(
                encounter.id(),
                participants.appointmentId(),
                encounter.status().name(),
                AttendedCustomerView.of(participants.customer(), encounter.context()),
                AttendingPractitionerView.of(participants.practitioner()),
                ProvidedServiceView.of(participants.service()),
                timing.startedAt(),
                timing.lastSavedAt(),
                timing.completedAt(),
                encounter.signedBy().map(SignatureView::of).orElse(null),
                encounter.sheet());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record AttendedCustomerView(
            UUID id, String name, LocalDate birthDate, CoverageView coverage, List<StandingAlertView> alerts) {

        static AttendedCustomerView of(AttendedCustomer customer, ClinicalContext context) {
            return new AttendedCustomerView(
                    customer.id(),
                    customer.name(),
                    customer.birthDate(),
                    context.covering().map(CoverageView::of).orElse(null),
                    context.standingAlerts().map(AttendedCustomerView::viewsOf).orElse(null));
        }

        private static List<StandingAlertView> viewsOf(List<ClinicalAlertSnapshot> alerts) {
            return alerts.stream().map(StandingAlertView::of).toList();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record AttendingPractitionerView(UUID id, String name, String license) {

        static AttendingPractitionerView of(AttendingPractitioner practitioner) {
            return new AttendingPractitionerView(practitioner.id(), practitioner.name(), practitioner.license());
        }
    }

    record ProvidedServiceView(UUID id, String name) {

        static ProvidedServiceView of(ProvidedService service) {
            return new ProvidedServiceView(service.id(), service.name());
        }
    }

    record SignatureView(UUID userId, String name) {

        static SignatureView of(Signature signature) {
            return new SignatureView(signature.userId(), signature.name());
        }
    }

}
