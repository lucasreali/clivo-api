package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.DeactivationReason;

record DeactivationRequest(String reason) {

    DeactivationReason toReason() {
        return new DeactivationReason(reason);
    }
}
