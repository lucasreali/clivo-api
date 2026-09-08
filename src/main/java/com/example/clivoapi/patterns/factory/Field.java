package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;

public interface Field {

    SheetField fill(RecordValues values);

    void check(RecordValues values);

    default void accept(RecordValues values) {
    }
}
