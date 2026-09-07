package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.RecordAssembly;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.clinical.ClinicalAlertService;
import com.example.clivoapi.core.clinical.ClinicalAlertSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ClinicalDisclosure {

    private final RoleAccess roleAccess;
    private final RecordAssembly records;
    private final AttachmentService attachments;
    private final ClinicalAlertService alerts;

    ClinicalDisclosure(
            RoleAccess roleAccess,
            RecordAssembly records,
            AttachmentService attachments,
            ClinicalAlertService alerts) {
        this.roleAccess = roleAccess;
        this.records = records;
        this.attachments = attachments;
        this.alerts = alerts;
    }

    public EncounterSnapshot fullyDisclose(Encounter encounter) {
        return encounter.snapshotWith(records.assemble(encounter.filling()));
    }

    public EncounterSnapshot discloseTo(Encounter encounter, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return fullyDisclose(encounter);
        }
        return encounter.summary();
    }

    public Optional<CustomerAttachments> filesOf(UUID customerId, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return Optional.of(attachments.of(customerId));
        }
        return Optional.empty();
    }

    public Optional<List<ClinicalAlertSnapshot>> alertsOf(UUID customerId, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return Optional.of(alerts.of(customerId));
        }
        return Optional.empty();
    }
}
