package com.example.clivoapi.core.clinical;

import com.example.clivoapi.common.text.TextField;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AlertNote(@Column(name = "note", nullable = false, length = 400) String value) {

    private static final TextField NOTE = new TextField("a clinical alert", 400);

    public AlertNote {
        value = NOTE.required(value);
    }

    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
