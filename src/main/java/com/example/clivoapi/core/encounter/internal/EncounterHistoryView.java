package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.core.encounter.AttachmentSnapshot;
import com.example.clivoapi.core.encounter.EncounterParticipants;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.example.clivoapi.core.encounter.HistoryEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record EncounterHistoryView(
        UUID id,
        UUID customerId,
        UUID practitionerId,
        String practitionerName,
        UUID serviceId,
        String serviceName,
        Instant startedAt,
        Instant completedAt,
        String status,
        ChargeView charge,
        CoverageView insurance,
        RecordSheet sheet,
        List<AttachmentView> attachments) {

    static EncounterHistoryView of(HistoryEntry entry) {
        EncounterSnapshot encounter = entry.encounter();
        EncounterParticipants participants = encounter.participants();
        return new EncounterHistoryView(
                encounter.id(),
                participants.customer().id(),
                participants.practitioner().id(),
                participants.practitioner().name(),
                participants.service().id(),
                participants.service().name(),
                encounter.timing().startedAt(),
                encounter.timing().completedAt(),
                encounter.status().name(),
                ChargeView.of(entry.charge()),
                entry.coveredBy().map(CoverageView::of).orElse(null),
                encounter.clinicalRecord().orElse(null),
                entry.files().map(EncounterHistoryView::viewsOf).orElse(null));
    }

    private static List<AttachmentView> viewsOf(List<AttachmentSnapshot> files) {
        return files.stream().map(AttachmentView::of).toList();
    }

}
