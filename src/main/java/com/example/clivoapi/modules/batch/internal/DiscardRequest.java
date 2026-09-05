package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.modules.inventory.MovementReason;

record DiscardRequest(String reason) {

    MovementReason toReason() {
        return new MovementReason(reason);
    }
}
