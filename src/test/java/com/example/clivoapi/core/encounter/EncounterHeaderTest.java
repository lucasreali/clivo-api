package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.clinical.AlertNote;
import com.example.clivoapi.core.clinical.ClinicalAlertService;
import com.example.clivoapi.support.Clinic;
import com.example.clivoapi.support.ClinicFixture;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EncounterHeaderTest extends ClinicFixture {

    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    @Autowired
    private ClinicalAlertService alerts;

    @Test
    void theHeaderCarriesWhoIsBeingSeenAndByWhom() {
        Clinic clinic = openClinic("TEST-HEADER-PARTIES");
        UUID id = openEncounter(clinic, anamnesis());

        EncounterParticipants participants = encounters.findOne(id, Role.PRACTITIONER).participants();

        assertThat(participants.customer().name()).isEqualTo("Ana Prado");
        assertThat(participants.customer().birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(participants.practitioner().name()).isEqualTo("Dra. Marina");
        assertThat(participants.service().name()).isEqualTo("Limpeza");
    }

    @Test
    void theHeaderCarriesTheStandingClinicalAlertsOfThePatient() {
        Clinic clinic = openClinic("TEST-HEADER-ALERTS");
        alerts.record(clinic.customerId(), new AlertNote("Alergia a penicilina"), Role.PRACTITIONER);
        UUID id = openEncounter(clinic, anamnesis());

        assertThat(encounters.findOne(id, Role.PRACTITIONER).context().standingAlerts())
                .get()
                .extracting(standing -> standing.getFirst().note())
                .isEqualTo("Alergia a penicilina");
    }

    @Test
    void receptionSeesTheHeaderWithoutTheClinicalAlertsOrTheRecord() {
        Clinic clinic = openClinic("TEST-HEADER-RECEPTION");
        alerts.record(clinic.customerId(), new AlertNote("Alergia a penicilina"), Role.PRACTITIONER);
        UUID id = openEncounter(clinic, anamnesis());
        change(ROLE_MODEL, "SEGREGATED");

        EncounterSnapshot seen = encounters.findOne(id, Role.RECEPTION);

        assertThat(seen.clinicalRecord()).isEmpty();
        assertThat(seen.context().standingAlerts()).isEmpty();
        assertThat(seen.participants().customer().name()).isEqualTo("Ana Prado");
    }

    @Test
    void savingTheDraftStampsWhenItWasLastSaved() {
        Clinic clinic = openClinic("TEST-HEADER-SAVED");
        UUID id = openEncounter(clinic, anamnesis());

        assertThat(encounters.findOne(id, Role.PRACTITIONER).timing().lastSavedAt()).isNull();

        encounters.fill(id, RecordValues.of(Map.of("complaint", "Dor no dente 26")));

        assertThat(encounters.findOne(id, Role.PRACTITIONER).timing().lastSavedAt()).isNotNull();
    }

    @Test
    void completingTheRecordSignsItWithWhoeverClosedIt() {
        Clinic clinic = openClinic("TEST-HEADER-SIGNED");
        UUID id = openEncounter(clinic, anamnesis());

        assertThat(encounters.findOne(id, Role.PRACTITIONER).signedBy()).isEmpty();

        encounters.complete(id, Role.PRACTITIONER);

        assertThat(encounters.findOne(id, Role.PRACTITIONER).signedBy())
                .get()
                .extracting(Signature::name)
                .isEqualTo("MANAGER");
    }

    @Test
    void aMalformedValueIsRefusedWhenTheDraftIsSavedAndNotOnlyOnCompletion() {
        Clinic clinic = openClinic("TEST-HEADER-EARLY");
        UUID id = openEncounter(clinic, scaleTemplate());

        assertThatThrownBy(() -> encounters.fill(id, RecordValues.of(Map.of("pain", "muito"))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field pain (pain) expects a number");
    }

    @Test
    void anIncompleteDraftIsStillSavedAndOnlyRefusedOnCompletion() {
        Clinic clinic = openClinic("TEST-HEADER-DRAFT");
        UUID id = openEncounter(clinic, requiredComplaint());

        encounters.fill(id, RecordValues.of(Map.of()));

        assertThatThrownBy(() -> encounters.complete(id, Role.PRACTITIONER))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field complaint (complaint) is required");
    }

    private UUID anamnesis() {
        return publishTemplate("Anamnesis", sectionWith("Complaint", fieldOf("complaint", "LONG_TEXT")));
    }

    private UUID scaleTemplate() {
        return publishTemplate("Pain", sectionWith("Complaint", fieldOf("pain", "SCALE")));
    }

    private UUID requiredComplaint() {
        return publishTemplate(
                "Required",
                sectionWith(
                        "Complaint",
                        new com.example.clivoapi.configuration.template.FieldContent(
                                "complaint", "complaint", "LONG_TEXT", null, true, java.util.List.of(),
                                Map.of(), null)));
    }
}
