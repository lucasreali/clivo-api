package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.ConsentPurpose;
import com.example.clivoapi.core.customer.ConsentStatement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record ConsentRequest(@NotBlank String purpose, @NotNull Boolean granted, @NotBlank String source) {

    ConsentStatement toStatement() {
        return new ConsentStatement(new ConsentPurpose(purpose), granted, source);
    }
}
