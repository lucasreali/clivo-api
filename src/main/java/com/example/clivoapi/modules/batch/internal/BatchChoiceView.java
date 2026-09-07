package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.common.extension.BatchChoice;
import java.util.UUID;

record BatchChoiceView(UUID batchId, String code, String warning) {

    static BatchChoiceView of(BatchChoice choice) {
        return new BatchChoiceView(choice.batchId(), choice.code(), choice.alert().orElse(null));
    }
}
