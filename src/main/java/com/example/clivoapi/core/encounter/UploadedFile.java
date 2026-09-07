package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.text.TextField;

public record UploadedFile(String fileName, String mimeType, FileContent content) {

    private static final TextField NAME = new TextField("an attachment file name", 160);

    private static final TextField TYPE = new TextField("an attachment content type", 80);

    public UploadedFile {
        fileName = NAME.required(fileName);
        mimeType = TYPE.required(mimeType);
    }

    public StoredFile describe() {
        return new StoredFile(fileName, mimeType, content.size());
    }
}
