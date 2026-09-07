package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.text.TextField;
import java.util.Objects;

public record TenantRegistration(String name, TenantProfile profile) {

    static final TextField NAME = new TextField("a clinic name", 120);

    public TenantRegistration {
        name = NAME.required(name);
        profile = Objects.requireNonNullElseGet(profile, TenantProfile::unknown);
    }
}
