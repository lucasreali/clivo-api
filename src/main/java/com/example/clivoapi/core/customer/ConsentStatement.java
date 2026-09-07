package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.text.TextField;

public record ConsentStatement(ConsentPurpose purpose, boolean granted, String source) {

    private static final TextField SOURCE = new TextField("a consent source", 30);

    public ConsentStatement {
        source = SOURCE.required(source);
    }
}
