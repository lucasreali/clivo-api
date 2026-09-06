package com.example.clivoapi.scenarios;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.support.Clinic;
import com.example.clivoapi.support.ClinicFixture;
import java.util.Map;
import org.springframework.context.annotation.Import;

@Import(ScenarioDatabase.class)
abstract class ScenarioTest extends ClinicFixture {

    protected static final String COMPLAINT = "complaint";

    protected Long anamnesisTemplate() {
        return publishTemplate("Anamnesis", sectionWith("Complaint", fieldOf(COMPLAINT, "LONG_TEXT")));
    }

    protected Long completeAnEncounter(Clinic clinic, Long templateId) {
        Long encounterId = openEncounter(clinic, templateId);
        encounters.fill(encounterId, RecordValues.of(Map.of(COMPLAINT, "Dor ao mastigar")));
        encounters.complete(encounterId, Role.PRACTITIONER);
        return encounterId;
    }
}
