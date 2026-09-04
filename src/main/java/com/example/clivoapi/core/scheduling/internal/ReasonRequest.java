package com.example.clivoapi.core.scheduling.internal;

import com.example.clivoapi.core.scheduling.CancellationReason;

record ReasonRequest(String reason) {

    CancellationReason toReason() {
        return new CancellationReason(reason);
    }
}
