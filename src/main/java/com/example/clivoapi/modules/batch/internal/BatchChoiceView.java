package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.common.extension.BatchChoice;

record BatchChoiceView(Long batchId, String code, String warning) {

    static BatchChoiceView of(BatchChoice choice) {
        return new BatchChoiceView(choice.batchId(), choice.code(), choice.alert().orElse(null));
    }
}
