package com.example.clivoapi.core.encounter;

import java.time.Instant;
import java.util.UUID;

public record AttachmentSnapshot(
        UUID id,
        UUID encounterId,
        String fileName,
        String mimeType,
        long sizeBytes,
        UUID authorId,
        String authorName,
        Instant uploadedAt) {
}
