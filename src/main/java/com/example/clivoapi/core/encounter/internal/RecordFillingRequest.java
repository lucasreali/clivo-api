package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.RecordValues;
import java.util.Map;

record RecordFillingRequest(Map<String, Object> values) {

    RecordValues toValues() {
        return RecordValues.of(values);
    }
}
