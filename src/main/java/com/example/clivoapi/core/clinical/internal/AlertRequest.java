package com.example.clivoapi.core.clinical.internal;

import com.example.clivoapi.core.clinical.AlertNote;
import io.swagger.v3.oas.annotations.media.Schema;

record AlertRequest(@Schema(example = "Alergia a penicilina") String note) {

    AlertNote toNote() {
        return new AlertNote(note);
    }
}
