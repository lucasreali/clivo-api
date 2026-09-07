package com.example.clivoapi.patterns.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.ComponentRegion;
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

    private static final String SEALANT = "selante";

    @AfterEach
    void narrowTheVocabularyBack() {
        jdbcTemplate.update("DELETE FROM component_mark WHERE code = ?", SEALANT);
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
                .containsExactly("mesial", "distal", "vestibular", "palatina", "incisal");

        ComponentRegion lowerMolar = regionOf(teeth, "46");
        assertThat(lowerMolar.position()).isEqualTo(6);
        assertThat(lowerMolar.groups()).containsExactlyInAnyOrderEntriesOf(Map.of("arch", "lower", "quadrant", "4"));
        assertThat(lowerMolar.parts()).containsExactly("mesial", "distal", "vestibular", "lingual", "oclusal");
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
                .contains("higido", "carie", "restaurado", "ausente");
        assertThat(teeth.vocabulary()).allSatisfy(mark -> assertThat(mark.rendering()).isNotBlank());
    }

    @Test
    void aToothOutsideTheChartRefusesTheCompletion() {
        UUID id = openWith("TEST-CHART-TOOTH", ODONTOGRAM, permanentChart());
        fill(id, marking("99", null, "carie"));

        assertThatThrownBy(() -> complete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the region 99");
    }

    @Test
    void aFaceThatDoesNotBelongToTheToothRefusesTheCompletion() {
        UUID id = openWith("TEST-CHART-FACE", ODONTOGRAM, permanentChart());
        fill(id, marking("11", "oclusal", "carie"));

        assertThatThrownBy(() -> complete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the part oclusal of the region 11");
    }

    @Test
    void aConditionOutsideTheVocabularyRefusesTheCompletion() {
        UUID id = openWith("TEST-CHART-WORD", ODONTOGRAM, permanentChart());
        fill(id, marking("26", "oclusal", "cariado"));

        assertThatThrownBy(() -> complete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the mark cariado");
    }

    @Test
    void theSameToothCarriesAMarkingOnEachOfItsFaces() {
        UUID id = openWith("TEST-CHART-FACES", ODONTOGRAM, permanentChart());
        fill(id, marking("26", "oclusal", "carie"), marking("26", "mesial", "restaurado"));

        assertThat(complete(id).isCompleted()).isTrue();
    }

    @Test
    void aConditionAddedToTheCatalogueIsAcceptedWithoutTouchingTheCode() {
        UUID id = openWith("TEST-CHART-CLINIC", ODONTOGRAM, permanentChart());
        fill(id, marking("26", "oclusal", SEALANT));

        assertThatThrownBy(() -> complete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the mark selante");

        widenTheVocabulary();
        assertThat(complete(id).isCompleted()).isTrue();
    }

    @Test
    void aFilledChartStaysReadableInTheVersionItWasRecordedUnder() {
        Clinic clinic = openClinic("TEST-CHART-VERSION");
        activate(ODONTOGRAM);
        UUID firstVersion = publishTemplate(LABEL, permanentChart());
        UUID id = openEncounter(clinic, firstVersion);
        fill(id, marking("26", "oclusal", "carie"));
        complete(id);

        templates.publish(templates.redefine(firstVersion, deciduousChart()).id());

        EncounterSnapshot recorded = reopen(id);
        assertThat(recorded.sheet().templateVersion()).isEqualTo(1);
        assertThat(chartOf(id).descriptor().regions()).hasSize(32);
        assertThat(chartOf(id).value()).isEqualTo(List.of(marking("26", "oclusal", "carie")));
    }

    @Test
    void theBodyMapDescribesTheRegionsDeclaredInTheTemplate() {
        SheetField map = chartOf(openWith("TEST-CHART-BODY", BODY_MAP, bodyMap()));
        ComponentDescriptor regions = map.descriptor();

        assertThat(map.fieldType()).isEqualTo("BODY_MAP");
        assertThat(regions.groupings()).isEmpty();
        assertThat(regions.codes()).containsExactly("ombro", "joelho");
        assertThat(regionOf(regions, "ombro").parts()).isEmpty();
        assertThat(regions.vocabulary()).extracting(ComponentMark::code).contains("dor", "edema");
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
    private void fill(UUID encounterId, Map<String, String>... markings) {
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

    private Map<String, String> marking(String region, String part, String mark) {
        return Optional.ofNullable(part)
                .map(face -> Map.of("region", region, "part", face, "mark", mark))
                .orElseGet(() -> Map.of("region", region, "mark", mark));
    }

    private void widenTheVocabulary() {
        jdbcTemplate.update(
                "INSERT INTO component_mark (id, component, code, label, rendering, sort_order) "
                        + "VALUES (gen_random_uuid(), 'ODONTOGRAM', ?, 'Selante', '#00695c', 11)",
                SEALANT);
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
