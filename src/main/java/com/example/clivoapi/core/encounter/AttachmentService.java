package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.encounter.internal.AttachmentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachments;
    private final EncounterBook encounters;
    private final AttachmentStorage storage;
    private final AccessService access;
    private final RoleAccess roleAccess;

    AttachmentService(
            AttachmentRepository attachments,
            EncounterBook encounters,
            AttachmentStorage storage,
            AccessService access,
            RoleAccess roleAccess) {
        this.attachments = attachments;
        this.encounters = encounters;
        this.storage = storage;
        this.access = access;
        this.roleAccess = roleAccess;
    }

    public AttachmentSnapshot attach(UUID encounterId, UploadedFile upload, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        Attachment attachment = new Attachment(
                encounters.reference(encounterId), upload, storage.store(upload.content()), access.signedIn());
        return attachments.save(attachment).snapshot();
    }

    @Transactional(readOnly = true)
    public AttachmentDownload download(UUID id, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        Attachment attachment = attachmentOf(id);
        return new AttachmentDownload(attachment.snapshot(), storage.retrieve(attachment.storedAt()));
    }

    @Transactional(readOnly = true)
    public CustomerAttachments visibleTo(UUID customerId, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        return of(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerAttachments of(UUID customerId) {
        return CustomerAttachments.of(attachments.findByEncounterCustomerIdOrderByUploadedAtDesc(customerId).stream()
                .map(Attachment::snapshot)
                .toList());
    }

    private Attachment attachmentOf(UUID id) {
        return attachments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attachment", id));
    }
}
