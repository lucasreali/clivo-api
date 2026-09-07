package com.example.clivoapi.core.encounter;

public interface AttachmentStorage {

    StorageKey store(FileContent content);

    FileContent retrieve(StorageKey key);
}
