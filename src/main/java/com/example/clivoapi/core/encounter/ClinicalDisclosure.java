package com.example.clivoapi.core.encounter;

import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClinicalDisclosure {

    private final RoleAccess roleAccess;
    private final DisclosedRecord record;
    private final EncounterContext context;
    private final AttachmentService attachments;

    ClinicalDisclosure(
            RoleAccess roleAccess,
            DisclosedRecord record,
            EncounterContext context,
            AttachmentService attachments) {
        this.roleAccess = roleAccess;
        this.record = record;
        this.context = context;
        this.attachments = attachments;
    }

    public EncounterSnapshot fullyDisclose(Encounter encounter) {
        return encounter.snapshotWith(record.of(encounter), context.of(encounter.customerId()));
    }

    public EncounterSnapshot discloseTo(Encounter encounter, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return fullyDisclose(encounter);
        }
        return encounter.snapshotWith(null, context.coverageOnlyOf(encounter.customerId()));
    }

    public EncounterSnapshot listedFor(Encounter encounter, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return encounter.snapshotWith(record.plainOf(encounter), ClinicalContext.undisclosed());
        }
        return encounter.summary();
    }

    public RecordComparison compare(Encounter encounter, Instant asOf, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        return record.compare(encounter, asOf);
    }

    public Optional<CustomerAttachments> filesOf(UUID customerId, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return Optional.of(attachments.of(customerId));
        }
        return Optional.empty();
    }

    public Optional<List<ClinicalAlertSnapshot>> alertsOf(UUID customerId, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return Optional.of(context.of(customerId).alerts());
        }
        return Optional.empty();
    }
}
