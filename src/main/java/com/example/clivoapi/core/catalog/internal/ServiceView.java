package com.example.clivoapi.core.catalog.internal;

import com.example.clivoapi.core.catalog.ServiceDetails;
import com.example.clivoapi.core.catalog.ServiceSnapshot;
import java.math.BigDecimal;
import java.util.UUID;

record ServiceView(UUID id, String name, short durationMinutes, BigDecimal price, String status) {

    static ServiceView of(ServiceSnapshot service) {
        ServiceDetails details = service.details();
        return new ServiceView(
                service.id(),
                details.name(),
                details.duration().minutes(),
                details.price().amount(),
                service.status().name());
    }
}
