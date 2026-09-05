package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.billing.RefundReason;

record RefundRequest(String reason) {

    RefundReason toReason() {
        return new RefundReason(reason);
    }
}
