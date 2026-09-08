package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.CoverageNote;
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
        CustomerView customer,
        PractitionerView practitioner,
        ServiceView service,
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
                CustomerView.of(participants.customer(), encounter.context()),
                PractitionerView.of(participants.practitioner()),
                ServiceView.of(participants.service()),
                timing.startedAt(),
                timing.lastSavedAt(),
                timing.completedAt(),
                encounter.signedBy().map(SignatureView::of).orElse(null),
                encounter.sheet());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record CustomerView(
            UUID id, String name, LocalDate birthDate, CoverageView coverage, List<AlertView> alerts) {

        static CustomerView of(AttendedCustomer customer, ClinicalContext context) {
            return new CustomerView(
                    customer.id(),
                    customer.name(),
                    customer.birthDate(),
                    context.covering().map(CoverageView::of).orElse(null),
                    context.standingAlerts().map(CustomerView::viewsOf).orElse(null));
        }

        private static List<AlertView> viewsOf(List<ClinicalAlertSnapshot> alerts) {
            return alerts.stream().map(AlertView::of).toList();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record PractitionerView(UUID id, String name, String license) {

        static PractitionerView of(AttendingPractitioner practitioner) {
            return new PractitionerView(practitioner.id(), practitioner.name(), practitioner.license());
        }
    }

    record ServiceView(UUID id, String name) {

        static ServiceView of(ProvidedService service) {
            return new ServiceView(service.id(), service.name());
        }
    }

    record SignatureView(UUID userId, String name) {

        static SignatureView of(Signature signature) {
            return new SignatureView(signature.userId(), signature.name());
        }
    }

    record CoverageView(String plan, String memberNumber) {

        static CoverageView of(CoverageNote note) {
            return new CoverageView(note.plan(), note.memberNumber());
        }
    }

    record AlertView(UUID id, String note, UUID authorId, String authorName, Instant recordedAt) {

        static AlertView of(ClinicalAlertSnapshot alert) {
            return new AlertView(
                    alert.id(), alert.note(), alert.authorId(), alert.authorName(), alert.recordedAt());
        }
    }
}
