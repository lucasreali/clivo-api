package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.common.tenant.Tenant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncounterServiceTest extends EncounterFixture {

    private Long templateId;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-ENCOUNTER");
        templateId = publishTemplate("Anamnesis", complaintWith("complaint", "LONG_TEXT"));
    }

    @Test
    void aWalkInEncounterStartsAsADraft() {
        EncounterSnapshot encounter = openWalkIn();

        assertThat(encounter.status()).isEqualTo(EncounterStatus.DRAFT);
        assertThat(encounter.participants().customerId()).isEqualTo(customerId());
        assertThat(encounter.participants().practitionerId()).isEqualTo(practitionerId());
        assertThat(encounter.completedAt()).isNull();
    }

    @Test
    void anEncounterOpenedFromAnAppointmentCarriesItsService() {
        Long appointmentId = bookAppointment();

        EncounterSnapshot encounter =
                encounters.open(EncounterOpening.forAppointment(appointmentId, templateId));

        assertThat(encounter.participants().appointmentId()).isEqualTo(appointmentId);
        assertThat(encounter.participants().serviceName()).isEqualTo("Limpeza");
        assertThat(encounter.participants().customerId()).isEqualTo(customerId());
    }

    @Test
    void filledValuesComeBackWhenTheEncounterIsReopened() {
        EncounterSnapshot draft = openWalkIn();

        encounters.fill(draft.id(), RecordValues.of(Map.of("complaint", "Dor no dente 26")));

        assertThat(fieldValuesOf(encounters.findOne(draft.id())))
                .containsEntry("complaint", "Dor no dente 26");
    }

    @Test
    void completingAnEncounterStampsItsCompletion() {
        EncounterSnapshot draft = openWalkIn();

        EncounterSnapshot completed = encounters.complete(draft.id());

        assertThat(completed.isCompleted()).isTrue();
        assertThat(completed.completedAt()).isNotNull();
    }

    @Test
    void aCompletedEncounterCannotBeFilledInAgain() {
        EncounterSnapshot draft = openWalkIn();
        encounters.complete(draft.id());

        assertThatThrownBy(() -> encounters.fill(draft.id(), RecordValues.of(Map.of("complaint", "Tarde demais"))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("an encounter in status COMPLETED cannot be filled in");
    }

    @Test
    void aCompletedEncounterKeepsTheTemplateVersionItWasFilledWith() {
        EncounterSnapshot draft = openWalkIn();
        encounters.fill(draft.id(), RecordValues.of(Map.of("complaint", "Dor no dente 26")));
        encounters.complete(draft.id());

        Long secondVersionId = templates.redefine(templateId, complaintWith("history", "LONG_TEXT")).id();
        templates.publish(secondVersionId);

        RecordSheet reopened = encounters.findOne(draft.id()).sheet();

        assertThat(reopened.templateId()).isEqualTo(templateId);
        assertThat(reopened.templateVersion()).isEqualTo(1);
        assertThat(fieldCodesOf(reopened)).containsExactly("complaint");
        assertThat(secondVersionId).isNotEqualTo(templateId);
    }

    @Test
    void anEncounterOfOneClinicDoesNotReachTheOther() {
        EncounterSnapshot draft = openWalkIn();
        Tenant other = createTenant("TEST-ENCOUNTER-OTHER");

        assertThatThrownBy(() -> valueInTenant(other, () -> encounters.findOne(draft.id())))
                .hasMessageContaining("Encounter");
    }

    private Map<String, Object> fieldValuesOf(EncounterSnapshot encounter) {
        return fieldsOf(encounter.sheet()).stream()
                .collect(HashMap::new, (values, field) -> values.put(field.code(), field.value()), HashMap::putAll);
    }

    private List<String> fieldCodesOf(RecordSheet sheet) {
        return fieldsOf(sheet).stream().map(SheetField::code).toList();
    }

    private List<SheetField> fieldsOf(RecordSheet sheet) {
        return sheet.sections().stream().flatMap(section -> section.fields().stream()).toList();
    }

    private EncounterSnapshot openWalkIn() {
        return encounters.open(EncounterOpening.walkIn(customerId(), practitionerId(), templateId));
    }
}
