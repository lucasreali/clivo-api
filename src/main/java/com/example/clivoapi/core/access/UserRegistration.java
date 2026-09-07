package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.text.TextField;

public record UserRegistration(String name, EmailAddress email, RawPassword password, Role role) {

    private static final TextField NAME = new TextField("a user name", 120);

    public UserRegistration {
        name = NAME.required(name);
        email = declared(email, "email");
        password = declared(password, "password");
        role = declared(role, "role");
    }

    private static <T> T declared(T value, String field) {
        if (value != null) {
            return value;
        }
        throw new BusinessException("%s is required".formatted(field));
    }
}
