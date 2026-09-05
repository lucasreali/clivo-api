package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.core.access.Role;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerHistoryTest extends EncounterFixture {

    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    @Autowired
    private ClinicParameterService parameters;

    private Long templateId;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-HISTORY");
        templateId = publishTemplate("Anamnesis", complaintWith("complaint", "LONG_TEXT"));
    }

    @Test
    void theHistoryComesBackNewestFirst() {
        Long older = completedEncounterSaying("Primeira consulta");
        Long newer = completedEncounterSaying("Retorno");

        List<EncounterSnapshot> history = encounters.historyOf(customerId(), Role.PRACTITIONER);

        assertThat(history).extracting(EncounterSnapshot::id).containsExactly(newer, older);
    }

    @Test
    void segregatedModeKeepsTheClinicalContentAwayFromReception() {
        completedEncounterSaying("Dor no dente 26");

        EncounterSnapshot seen = onlyEntrySeenBy(Role.RECEPTION);

        assertThat(seen.clinicalRecord()).isEmpty();
        assertThat(seen.participants().practitionerName()).isEqualTo("Dr. Marina");
        assertThat(seen.completedAt()).isNotNull();
    }

    @Test
    void segregatedModeShowsTheClinicalContentToThePractitioner() {
        completedEncounterSaying("Dor no dente 26");

        assertThat(onlyEntrySeenBy(Role.PRACTITIONER).clinicalRecord()).isPresent();
    }

    @Test
    void singleModeShowsTheClinicalContentToReception() {
        completedEncounterSaying("Dor no dente 26");
        parameters.change(ROLE_MODEL, ParameterValue.of("SINGLE"));

        assertThat(onlyEntrySeenBy(Role.RECEPTION).clinicalRecord()).isPresent();
    }

    @Test
    void readingOneEncounterByIdObeysTheSameScopeAsTheHistory() {
        Long id = completedEncounterSaying("Dor no dente 26");

        assertThat(encounters.findOne(id, Role.RECEPTION).clinicalRecord()).isEmpty();
        assertThat(encounters.findOne(id, Role.PRACTITIONER).clinicalRecord()).isPresent();
    }

    @Test
    void completingAnEncounterAnswersReceptionWithoutTheClinicalContent() {
        Long id = encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), templateId))
                .id();
        encounters.fill(id, RecordValues.of(Map.of("complaint", "Dor no dente 26")));

        assertThat(encounters.complete(id, Role.RECEPTION).clinicalRecord()).isEmpty();
    }

    private EncounterSnapshot onlyEntrySeenBy(Role viewer) {
        List<EncounterSnapshot> history = encounters.historyOf(customerId(), viewer);
        return history.getFirst();
    }

    private Long completedEncounterSaying(String complaint) {
        Long id = encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), templateId))
                .id();
        encounters.fill(id, RecordValues.of(Map.of("complaint", complaint)));
        return completeAsPractitioner(id).id();
    }
}
