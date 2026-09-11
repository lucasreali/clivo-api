package com.example.clivoapi.patterns.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.ComponentRegion;
import com.example.clivoapi.common.extension.MarkedRegionState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.example.clivoapi.support.Clinic;
import com.example.clivoapi.support.ClinicFixture;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SpecialComponentTest extends ClinicFixture {

    private static final ModuleCode ODONTOGRAM = new ModuleCode("odontogram");

    private static final ModuleCode BODY_MAP = new ModuleCode("bodymap");

    private static final String CHART = "chart";

    private static final String LABEL = "Dental chart";

    private static final String VENEER = "veneer";

    @AfterEach
    void narrowTheVocabularyBack() {
        jdbcTemplate.update("DELETE FROM component_mark WHERE code = ?", VENEER);
    }

    @Test
    void publishingAnOdontogramWithoutItsModuleIsRefused() {
        openClinic("TEST-CHART-GATE");
        UUID draftId = templates.draft(LABEL, null, permanentChart()).id();

        assertThatThrownBy(() -> templates.publish(draftId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("module odontogram is not active in this clinic");
    }

    @Test
    void theChartDeclaresEveryPermanentToothWithItsArchQuadrantAndFaces() {
        SheetField chart = chartOf(openWith("TEST-CHART-FULL", ODONTOGRAM, permanentChart()));
        ComponentDescriptor teeth = chart.descriptor();

        assertThat(chart.fieldType()).isEqualTo("ODONTOGRAM");
        assertThat(teeth.groupings()).containsExactly("arch", "quadrant");
        assertThat(teeth.codes()).hasSize(32).startsWith("11", "12").endsWith("47", "48");
        ComponentRegion upperIncisor = regionOf(teeth, "11");
        assertThat(upperIncisor.position()).isEqualTo(1);
        assertThat(upperIncisor.groups()).containsExactlyInAnyOrderEntriesOf(Map.of("arch", "upper", "quadrant", "1"));
        assertThat(upperIncisor.parts())
                .containsExactly("mesial", "distal", "vestibular", "palatal", "incisal");

        ComponentRegion lowerMolar = regionOf(teeth, "46");
        assertThat(lowerMolar.position()).isEqualTo(6);
        assertThat(lowerMolar.groups()).containsExactlyInAnyOrderEntriesOf(Map.of("arch", "lower", "quadrant", "4"));
        assertThat(lowerMolar.parts()).containsExactly("mesial", "distal", "vestibular", "lingual", "occlusal");
    }

    @Test
    void aDeciduousChartDeclaresTwentyTeethAndAPermanentOneThirtyTwo() {
        assertThat(chartOf(openWith("TEST-CHART-ADULT", ODONTOGRAM, permanentChart()))
                        .descriptor()
                        .regions())
                .hasSize(32);
        assertThat(chartOf(openWith("TEST-CHART-CHILD", ODONTOGRAM, deciduousChart()))
                        .descriptor()
                        .codes())
                .hasSize(20)
                .startsWith("51", "52")
                .endsWith("84", "85");
    }

    @Test
    void theChartCarriesTheVocabularyItAccepts() {
        ComponentDescriptor teeth =
                chartOf(openWith("TEST-CHART-WORDS", ODONTOGRAM, permanentChart())).descriptor();

        assertThat(teeth.vocabulary())
                .extracting(ComponentMark::code)
                .contains("healthy", "caries", "restored", "missing", "sealant");
        assertThat(teeth.vocabulary()).allSatisfy(mark -> assertThat(mark.rendering()).isNotBlank());
        assertThat(teeth.vocabulary()).allSatisfy(mark -> assertThat(mark.appliesTo()).isNotNull());
    }

    @Test
    void aToothOutsideTheChartIsRefusedWhenTheDraftIsSaved() {
        UUID id = openWith("TEST-CHART-TOOTH", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() -> fill(id, marking("99", "occlusal", "caries")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the region 99");
    }

    @Test
    void aFaceThatDoesNotBelongToTheToothIsRefusedWhenTheDraftIsSaved() {
        UUID id = openWith("TEST-CHART-FACE", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() -> fill(id, marking("11", "occlusal", "caries")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the part occlusal of the region 11");
    }

    @Test
    void aConditionOutsideTheVocabularyIsRefusedWhenTheDraftIsSaved() {
        UUID id = openWith("TEST-CHART-WORD", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() -> fill(id, marking("26", "occlusal", "decayed")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the mark decayed");
    }

    @Test
    void aWholeToothConditionIsRefusedOnASingleFace() {
        UUID id = openWith("TEST-CHART-TARGET", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() -> fill(id, marking("26", "occlusal", "missing")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) marks 26 with missing, "
                        + "which applies to the region as a whole, with no part");
    }

    @Test
    void aFaceConditionIsRefusedOnTheWholeTooth() {
        UUID id = openWith("TEST-CHART-FACEONLY", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() -> fill(id, markingOn("26", List.of(), "caries")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) marks 26 with caries, "
                        + "which applies to at least one part of the region");
    }

    @Test
    void aConditionThatFitsEitherTargetIsAcceptedOnBoth() {
        UUID id = openWith("TEST-CHART-ANY", ODONTOGRAM, permanentChart());

        fill(id, markingOn("26", List.of(), "fracture"), marking("27", "occlusal", "fracture"));

        assertThat(complete(id).isCompleted()).isTrue();
    }

    @Test
    void oneMarkingCoversSeveralFacesOfTheSameTooth() {
        UUID id = openWith("TEST-CHART-MULTI", ODONTOGRAM, permanentChart());

        fill(id, markingOn("36", List.of("occlusal", "lingual"), "caries"));

        assertThat(chartOf(id).markings())
                .extracting(MarkedRegionState::part)
                .containsExactly("occlusal", "lingual");
    }

    @Test
    void theSameFaceCannotBeMarkedTwiceInOneSession() {
        UUID id = openWith("TEST-CHART-TWICE", ODONTOGRAM, permanentChart());

        assertThatThrownBy(() ->
                        fill(id, marking("26", "occlusal", "caries"), marking("26", "occlusal", "restored")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) marks the part occlusal of the region 26 twice");
    }

    @Test
    void aNoteRidesAlongWithTheMarkingThatCarriesIt() {
        UUID id = openWith("TEST-CHART-NOTE", ODONTOGRAM, permanentChart());

        fill(id, noted(marking("26", "occlusal", "restored"), "Active caries removed and restored"));

        assertThat(chartOf(id).markings())
                .singleElement()
                .extracting(MarkedRegionState::note)
                .isEqualTo("Active caries removed and restored");
    }

    @Test
    void theSameToothCarriesAMarkingOnEachOfItsFaces() {
        UUID id = openWith("TEST-CHART-FACES", ODONTOGRAM, permanentChart());
        fill(id, marking("26", "occlusal", "caries"), marking("26", "mesial", "restored"));

        assertThat(complete(id).isCompleted()).isTrue();
    }

    @Test
    void aConditionAddedToTheCatalogueIsAcceptedWithoutTouchingTheCode() {
        UUID id = openWith("TEST-CHART-CLINIC", ODONTOGRAM, permanentChart());
        assertThatThrownBy(() -> fill(id, marking("26", "occlusal", VENEER)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the mark veneer");

        widenTheVocabulary();
        fill(id, marking("26", "occlusal", VENEER));

        assertThat(complete(id).isCompleted()).isTrue();
    }

    @Test
    void aFilledChartStaysReadableInTheVersionItWasRecordedUnder() {
        Clinic clinic = openClinic("TEST-CHART-VERSION");
        activate(ODONTOGRAM);
        UUID firstVersion = publishTemplate(LABEL, permanentChart());
        UUID id = openEncounter(clinic, firstVersion);
        fill(id, marking("26", "occlusal", "caries"));
        complete(id);

        templates.publish(templates.redefine(firstVersion, deciduousChart()).id());

        EncounterSnapshot recorded = reopen(id);
        assertThat(recorded.sheet().templateVersion()).isEqualTo(1);
        assertThat(chartOf(id).descriptor().regions()).hasSize(32);
        assertThat(chartOf(id).value()).isEqualTo(List.of(marking("26", "occlusal", "caries")));
        assertThat(chartOf(id).markings()).hasSize(1);
    }

    @Test
    void theBodyMapDescribesTheRegionsDeclaredInTheTemplate() {
        SheetField map = chartOf(openWith("TEST-CHART-BODY", BODY_MAP, bodyMap()));
        ComponentDescriptor regions = map.descriptor();

        assertThat(map.fieldType()).isEqualTo("BODY_MAP");
        assertThat(regions.groupings()).isEmpty();
        assertThat(regions.codes()).containsExactly("ombro", "joelho");
        assertThat(regionOf(regions, "ombro").parts()).isEmpty();
        assertThat(regions.vocabulary()).extracting(ComponentMark::code).contains("pain", "swelling");
    }

    @Test
    void theComponentLeavesNoTraceOnceItsModuleIsSwitchedOff() {
        UUID id = openWith("TEST-CHART-OFF", ODONTOGRAM, permanentChart());

        modules.deactivate(ODONTOGRAM);

        assertThat(fieldsOf(id)).isEmpty();
    }

    private UUID openWith(String clinicName, ModuleCode module, TemplateContent content) {
        Clinic clinic = openClinic(clinicName);
        activate(module);
        return openEncounter(clinic, publishTemplate(LABEL, content));
    }

    @SafeVarargs
    private void fill(UUID encounterId, Map<String, Object>... markings) {
        encounters.fill(encounterId, RecordValues.of(Map.of(CHART, List.of(markings))));
    }

    private EncounterSnapshot complete(UUID encounterId) {
        return encounters.complete(encounterId, Role.PRACTITIONER);
    }

    private EncounterSnapshot reopen(UUID encounterId) {
        return encounters.findOne(encounterId, Role.PRACTITIONER);
    }

    private SheetField chartOf(UUID encounterId) {
        return fieldsOf(encounterId).getFirst();
    }

    private List<SheetField> fieldsOf(UUID encounterId) {
        return reopen(encounterId).sheet().sections().getFirst().fields();
    }

    private ComponentRegion regionOf(ComponentDescriptor descriptor, String code) {
        return descriptor.regionNamed(code).orElseThrow();
    }

    private Map<String, Object> marking(String region, String part, String mark) {
        return Optional.ofNullable(part)
                .map(face -> markingOn(region, List.of(face), mark))
                .orElseGet(() -> Map.of("region", region, "mark", mark));
    }

    private Map<String, Object> markingOn(String region, List<String> parts, String mark) {
        return Map.of("region", region, "parts", parts, "mark", mark);
    }

    private Map<String, Object> noted(Map<String, Object> marking, String note) {
        Map<String, Object> written = new LinkedHashMap<>(marking);
        written.put("note", note);
        return written;
    }

    private void widenTheVocabulary() {
        jdbcTemplate.update(
                "INSERT INTO component_mark (id, component, code, label, rendering, sort_order, applies_to) "
                        + "VALUES (gen_random_uuid(), 'ODONTOGRAM', ?, 'Veneer', '#00695c', 12, 'PART')",
                VENEER);
    }

    private TemplateContent permanentChart() {
        return chart("ODONTOGRAM", ODONTOGRAM, List.of(), Map.of());
    }

    private TemplateContent deciduousChart() {
        return chart("ODONTOGRAM", ODONTOGRAM, List.of(), Map.of("variant", "deciduous"));
    }

    private TemplateContent bodyMap() {
        return chart("BODY_MAP", BODY_MAP, List.of("ombro", "joelho"), Map.of());
    }

    private TemplateContent chart(
            String component, ModuleCode module, List<String> options, Map<String, Object> settings) {
        return new TemplateContent(List.of(new SectionContent(
                "Exam",
                List.of(new FieldContent(
                        CHART, LABEL, "COMPONENT", component, false, options, settings, module.value())))));
    }
}
