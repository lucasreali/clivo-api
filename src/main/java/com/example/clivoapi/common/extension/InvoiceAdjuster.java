package com.example.clivoapi.common.extension;

import java.util.Optional;

public interface InvoiceAdjuster {

    Optional<InvoiceAdjustment> adjustmentFor(BillableEncounter encounter);
}
