package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record StorageKey(@Column(name = "object_key", nullable = false, length = 255) String value) {

    public StorageKey {
        if (value == null || value.isBlank()) {
            throw new BusinessException("a stored file is reachable by a storage key");
        }
    }

    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
