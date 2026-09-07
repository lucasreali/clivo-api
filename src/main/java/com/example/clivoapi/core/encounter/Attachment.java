package com.example.clivoapi.core.encounter;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.access.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "attachment")
public class Attachment extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false, updatable = false)
    private Encounter encounter;

    @Embedded
    private StoredFile file;

    @Embedded
    private StorageKey key;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false, updatable = false)
    private AppUser author;

    protected Attachment() {
    }

    public Attachment(Encounter encounter, UploadedFile upload, StorageKey key, AppUser author) {
        this.encounter = encounter;
        this.file = upload.describe();
        this.key = key;
        this.author = author;
        this.uploadedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public StorageKey storedAt() {
        return key;
    }

    public AttachmentSnapshot snapshot() {
        return new AttachmentSnapshot(
                id,
                encounter.id(),
                file.fileName(),
                file.mimeType(),
                file.sizeBytes(),
                author.id(),
                author.name(),
                uploadedAt);
    }
}
