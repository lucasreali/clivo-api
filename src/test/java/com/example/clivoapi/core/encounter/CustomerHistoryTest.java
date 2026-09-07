package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.clinical.AlertNote;
import com.example.clivoapi.core.clinical.ClinicalAlertService;
import com.example.clivoapi.common.tenant.Tenant;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerHistoryTest extends EncounterFixture {

    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    private static final String SINGLE = "SINGLE";

    @Autowired
    private ClinicParameterService parameters;

    @Autowired
    private AttachmentService attachments;

    @Autowired
    private ClinicalAlertService alerts;

    private UUID templateId;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-HISTORY");
        signIn(Role.PRACTITIONER);
        templateId = publishTemplate("Anamnesis", complaintWith("complaint", "LONG_TEXT"));
    }

    @Test
    void theHistoryComesBackNewestFirst() {
        UUID older = completedEncounterSaying("Primeira consulta");
        UUID newer = completedEncounterSaying("Retorno");

        List<HistoryEntry> entries = historySeenBy(Role.PRACTITIONER).entries();

        assertThat(entries).extracting(entry -> entry.encounter().id()).containsExactly(newer, older);
    }

    @Test
    void segregatedModeKeepsTheClinicalContentAwayFromReception() {
        UUID encounterId = completedEncounterSaying("Dor no dente 26");
        attach(encounterId, "radiografia.png");
        recordAnAlert();

        CustomerHistory history = historySeenBy(Role.RECEPTION);

        assertThat(onlyEntryOf(history).encounter().clinicalRecord()).isEmpty();
        assertThat(onlyEntryOf(history).files()).isEmpty();
        assertThat(history.standingAlerts()).isEmpty();
        assertThat(onlyEntryOf(history).encounter().participants().practitionerName()).isEqualTo("Dr. Marina");
        assertThat(onlyEntryOf(history).encounter().completedAt()).isNotNull();
    }

    @Test
    void singleModeShowsTheWholeClinicalContentToReception() {
        UUID encounterId = completedEncounterSaying("Dor no dente 26");
        attach(encounterId, "radiografia.png");
        recordAnAlert();
        parameters.change(ROLE_MODEL, ParameterValue.of(SINGLE));

        CustomerHistory history = historySeenBy(Role.RECEPTION);

        assertThat(onlyEntryOf(history).encounter().clinicalRecord()).isPresent();
        assertThat(onlyEntryOf(history).files()).hasValueSatisfying(files ->
                assertThat(files).extracting(AttachmentSnapshot::fileName).containsExactly("radiografia.png"));
        assertThat(history.standingAlerts()).hasValueSatisfying(standing ->
                assertThat(standing).extracting(alert -> alert.note()).containsExactly("Alergia a penicilina"));
    }

    @Test
    void thePractitionerSeesEverythingUnderEitherRoleModel() {
        UUID encounterId = completedEncounterSaying("Dor no dente 26");
        attach(encounterId, "radiografia.png");
        recordAnAlert();

        assertThatThePractitionerSeesEverything();
        parameters.change(ROLE_MODEL, ParameterValue.of(SINGLE));
        assertThatThePractitionerSeesEverything();
    }

    @Test
    void theStatusCountsMatchTheEncountersReturned() {
        completedEncounterSaying("Retorno");
        openAnEncounter();

        CustomerHistory history = historySeenBy(Role.PRACTITIONER);

        assertThat(history.counts().total()).isEqualTo(history.entries().size());
        assertThat(history.counts().of(EncounterStatus.COMPLETED)).isEqualTo(1);
        assertThat(history.counts().of(EncounterStatus.DRAFT)).isEqualTo(1);
        assertThat(history.counts().of(EncounterStatus.CANCELLED)).isZero();
    }

    @Test
    void theHistoryTellsSinceWhenTheCustomerBelongsToTheClinic() {
        completedEncounterSaying("Retorno");

        assertThat(historySeenBy(Role.PRACTITIONER).customerSince()).isNotNull();
    }

    @Test
    void receptionIsRefusedTheAttachmentsOfACustomerUnderSegregatedRoles() {
        attach(completedEncounterSaying("Dor no dente 26"), "radiografia.png");

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> attachments.visibleTo(customerId(), Role.RECEPTION));
    }

    @Test
    void anAttachmentOfAnotherClinicIsNotReachableByIdentifier() {
        UUID kept = attach(completedEncounterSaying("Dor no dente 26"), "radiografia.png");

        Tenant other = openAnotherClinic();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> valueInTenant(other, () -> attachments.download(kept, Role.PRACTITIONER)));
    }

    @Test
    void readingOneEncounterByIdObeysTheSameScopeAsTheHistory() {
        UUID id = completedEncounterSaying("Dor no dente 26");

        assertThat(encounters.findOne(id, Role.RECEPTION).clinicalRecord()).isEmpty();
        assertThat(encounters.findOne(id, Role.PRACTITIONER).clinicalRecord()).isPresent();
    }

    @Test
    void completingAnEncounterAnswersReceptionWithoutTheClinicalContent() {
        UUID id = openAnEncounter();
        encounters.fill(id, RecordValues.of(Map.of("complaint", "Dor no dente 26")));

        assertThat(encounters.complete(id, Role.RECEPTION).clinicalRecord()).isEmpty();
    }

    private void assertThatThePractitionerSeesEverything() {
        CustomerHistory history = historySeenBy(Role.PRACTITIONER);
        assertThat(onlyEntryOf(history).encounter().clinicalRecord()).isPresent();
        assertThat(onlyEntryOf(history).files()).isPresent();
        assertThat(history.standingAlerts()).isPresent();
    }

    private Tenant openAnotherClinic() {
        Tenant kept = clinic();
        Tenant other = createTenant("TEST-HISTORY-OTHER");
        bindTenant(kept);
        return other;
    }

    private CustomerHistory historySeenBy(Role viewer) {
        return encounters.historyOf(customerId(), viewer);
    }

    private HistoryEntry onlyEntryOf(CustomerHistory history) {
        return history.entries().getFirst();
    }

    private void recordAnAlert() {
        alerts.record(customerId(), new AlertNote("Alergia a penicilina"), Role.PRACTITIONER);
    }

    private UUID attach(UUID encounterId, String fileName) {
        UploadedFile upload = new UploadedFile(
                fileName, "image/png", new FileContent("radiografia".getBytes(StandardCharsets.UTF_8)));
        return attachments.attach(encounterId, upload, Role.PRACTITIONER).id();
    }

    private UUID openAnEncounter() {
        return encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();
    }

    private UUID completedEncounterSaying(String complaint) {
        UUID id = openAnEncounter();
        encounters.fill(id, RecordValues.of(Map.of("complaint", complaint)));
        return completeAsPractitioner(id).id();
    }
}
