package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.BusinessException;

public record FileContent(byte[] bytes) {

    public FileContent {
        bytes = present(bytes);
    }

    public long size() {
        return bytes.length;
    }

    private static byte[] present(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException("an attachment carries a file with content");
        }
        return bytes;
    }
}
