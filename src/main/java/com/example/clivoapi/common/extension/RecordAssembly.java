package com.example.clivoapi.common.extension;

public interface RecordAssembly {

    RecordSheet assemble(RecordFilling filling);

    void validate(RecordFilling filling);
}
