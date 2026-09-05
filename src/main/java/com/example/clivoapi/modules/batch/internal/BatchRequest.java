package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.modules.batch.BatchCode;
import com.example.clivoapi.modules.batch.BatchDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

record BatchRequest(
        @NotBlank String code, LocalDate expiresOn, @NotNull BigDecimal quantity, String manufacturer) {

    BatchDetails toDetails() {
        return new BatchDetails(new BatchCode(code), expiresOn, new Quantity(quantity), manufacturer);
    }
}
