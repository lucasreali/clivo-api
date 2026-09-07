package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.core.encounter.AttachmentStorage;
import com.example.clivoapi.core.encounter.FileContent;
import com.example.clivoapi.core.encounter.StorageKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class FilesystemAttachmentStorage implements AttachmentStorage {

    private final Path root;

    FilesystemAttachmentStorage(
            @Value("${clivo.attachments.directory:${java.io.tmpdir}/clivo-attachments}") Path root) {
        this.root = root;
    }

    @Override
    public StorageKey store(FileContent content) {
        StorageKey key = new StorageKey(UUID.randomUUID().toString());
        write(key, content);
        return key;
    }

    @Override
    public FileContent retrieve(StorageKey key) {
        try {
            return new FileContent(Files.readAllBytes(fileFor(key)));
        } catch (IOException failure) {
            throw new BusinessException("the stored file %s could not be read".formatted(key));
        }
    }

    private void write(StorageKey key, FileContent content) {
        try {
            Files.createDirectories(root);
            Files.write(fileFor(key), content.bytes());
        } catch (IOException failure) {
            throw new BusinessException("the file could not be stored under %s".formatted(root));
        }
    }

    private Path fileFor(StorageKey key) {
        return root.resolve(key.asText());
    }
}
