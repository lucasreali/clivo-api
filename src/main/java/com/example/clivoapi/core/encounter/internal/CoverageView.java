package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.common.extension.CoverageNote;

record CoverageView(String plan, String memberNumber) {

    static CoverageView of(CoverageNote note) {
        return new CoverageView(note.plan(), note.memberNumber());
    }
}
