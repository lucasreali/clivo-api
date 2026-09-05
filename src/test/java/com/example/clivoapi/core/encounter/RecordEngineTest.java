package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.RecordSheet;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.common.extension.SheetSection;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordEngineTest extends EncounterFixture {

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-RECORD-ENGINE");
    }

    @Test
    void theSheetFollowsTheTemplateAndCarriesTheFilledValues() {
        Long id = openWith(fullTemplate());

        EncounterSnapshot filled = encounters.fill(id, RecordValues.of(Map.of("weight", 12.5, "species", "Cao")));

        RecordSheet sheet = filled.sheet();
        assertThat(sheet.sections()).extracting(SheetSection::name).containsExactly("Vitals", "Profile");
        assertThat(codesOf(sheet)).containsExactly("weight", "seenOn", "species", "notes");
        assertThat(valueOf(sheet, "weight")).isEqualTo(12.5);
        assertThat(valueOf(sheet, "notes")).isNull();
    }

    @Test
    void aRequiredFieldLeftEmptyRefusesTheCompletion() {
        Long id = openWith(fullTemplate());

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field weight (Weight) is required");
    }

    @Test
    void aNumberOutsideItsRangeRefusesTheCompletion() {
        Long id = openWith(fullTemplate());
        encounters.fill(id, RecordValues.of(Map.of("weight", 900, "species", "Cao")));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field weight (Weight) accepts no value above 300");
    }

    @Test
    void aChoiceOutsideTheDeclaredOptionsRefusesTheCompletion() {
        Long id = openWith(fullTemplate());
        encounters.fill(id, RecordValues.of(Map.of("weight", 12.5, "species", "Dragao")));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field species (Species) accepts only one of [Cao, Gato]");
    }

    @Test
    void aTextLongerThanItsLimitRefusesTheCompletion() {
        Long id = openWith(fullTemplate());
        encounters.fill(id, RecordValues.of(Map.of("weight", 12.5, "species", "Cao", "notes", "x".repeat(41))));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field notes (Notes) accepts at most 40 characters");
    }

    @Test
    void aDateWrittenTheWrongWayRefusesTheCompletion() {
        Long id = openWith(fullTemplate());
        encounters.fill(id, RecordValues.of(Map.of("weight", 12.5, "species", "Cao", "seenOn", "31/12/2026")));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field seenOn (Seen on) expects a date written as yyyy-MM-dd");
    }

    @Test
    void aFullyFilledSheetIsAccepted() {
        Long id = openWith(fullTemplate());
        encounters.fill(id, RecordValues.of(Map.of("weight", 12.5, "species", "Cao", "seenOn", "2026-12-31")));

        assertThat(completeAsPractitioner(id).isCompleted()).isTrue();
    }

    @Test
    void aFieldTypeWithoutAFactoryIsRefused() {
        TemplateContent unknownType = new TemplateContent(List.of(new SectionContent(
                "Vitals",
                List.of(new FieldContent("mood", "Mood", "EMOJI_SCALE", null, false, List.of(), Map.of(), null)))));

        assertThatThrownBy(() -> openWith(unknownType))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field mood (Mood) declares the unknown type EMOJI_SCALE");
    }

    private Long openWith(TemplateContent content) {
        Long templateId = publishTemplate("Consultation", content);
        return encounters.open(EncounterOpening.walkIn(customerId(), practitionerId(), templateId)).id();
    }

    private TemplateContent fullTemplate() {
        return new TemplateContent(List.of(
                new SectionContent(
                        "Vitals",
                        List.of(
                                new FieldContent(
                                        "weight",
                                        "Weight",
                                        "DECIMAL",
                                        null,
                                        true,
                                        List.of(),
                                        Map.of("min", 0, "max", 300),
                                        null),
                                new FieldContent("seenOn", "Seen on", "DATE", null, false, List.of(), Map.of(), null))),
                new SectionContent(
                        "Profile",
                        List.of(
                                new FieldContent(
                                        "species",
                                        "Species",
                                        "SINGLE_CHOICE",
                                        null,
                                        true,
                                        List.of("Cao", "Gato"),
                                        Map.of(),
                                        null),
                                new FieldContent(
                                        "notes",
                                        "Notes",
                                        "LONG_TEXT",
                                        null,
                                        false,
                                        List.of(),
                                        Map.of("maxLength", 40),
                                        null)))));
    }

    private List<String> codesOf(RecordSheet sheet) {
        return fieldsOf(sheet).stream().map(SheetField::code).toList();
    }

    private Object valueOf(RecordSheet sheet, String code) {
        return fieldsOf(sheet).stream()
                .filter(field -> field.code().equals(code))
                .findFirst()
                .map(SheetField::value)
                .orElse(null);
    }

    private List<SheetField> fieldsOf(RecordSheet sheet) {
        return sheet.sections().stream()
                .flatMap(section -> section.fields().stream())
                .toList();
    }
}
