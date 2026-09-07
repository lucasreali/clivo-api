package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.encounter.AttachmentSnapshot;
import java.time.Instant;
import java.util.UUID;

record AttachmentView(
        UUID id,
        UUID encounterId,
        String fileName,
        String mimeType,
        long sizeBytes,
        UUID authorId,
        String authorName,
        Instant uploadedAt) {

    static AttachmentView of(AttachmentSnapshot attachment) {
        return new AttachmentView(
                attachment.id(),
                attachment.encounterId(),
                attachment.fileName(),
                attachment.mimeType(),
                attachment.sizeBytes(),
                attachment.authorId(),
                attachment.authorName(),
                attachment.uploadedAt());
    }
}
