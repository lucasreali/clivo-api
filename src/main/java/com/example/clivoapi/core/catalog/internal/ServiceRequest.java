package com.example.clivoapi.core.catalog.internal;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.catalog.ServiceDetails;
import com.example.clivoapi.core.catalog.ServiceDuration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record ServiceRequest(@NotBlank String name, @NotNull Short durationMinutes, @NotNull BigDecimal price) {

    ServiceDetails toDetails() {
        return new ServiceDetails(name, new ServiceDuration(durationMinutes), new Money(price));
    }
}
