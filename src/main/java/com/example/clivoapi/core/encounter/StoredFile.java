package com.example.clivoapi.core.encounter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record StoredFile(
        @Column(name = "file_name", nullable = false, length = 160) String fileName,
        @Column(name = "mime_type", nullable = false, length = 80) String mimeType,
        @Column(name = "size_bytes", nullable = false) long sizeBytes) {
}
