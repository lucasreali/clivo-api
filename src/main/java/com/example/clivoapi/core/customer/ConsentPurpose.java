package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.text.TextField;
import java.util.Locale;

public record ConsentPurpose(String value) {

    private static final String DATA_PROCESSING = "DATA_PROCESSING";

    private static final TextField PURPOSE = new TextField("a consent purpose", 60);

    public ConsentPurpose {
        value = PURPOSE.required(value).toUpperCase(Locale.ROOT);
    }

    public static ConsentPurpose dataProcessing() {
        return new ConsentPurpose(DATA_PROCESSING);
    }

    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
