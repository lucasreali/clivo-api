package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.modules.batch.BatchDetails;
import com.example.clivoapi.modules.batch.BatchSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;

record BatchView(
        Long id,
        Long productId,
        String productName,
        String code,
        LocalDate expiresOn,
        BigDecimal quantity,
        String manufacturer,
        boolean expired,
        String status) {

    static BatchView of(BatchSnapshot batch) {
        BatchDetails details = batch.details();
        return new BatchView(
                batch.id(),
                batch.productId(),
                batch.productName(),
                details.code().asText(),
                details.expiresOn(),
                details.quantity().amount(),
                details.madeBy().orElse(null),
                batch.expired(),
                batch.status().name());
    }
}
