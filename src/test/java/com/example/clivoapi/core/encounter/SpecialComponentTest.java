package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SpecialComponentTest extends EncounterFixture {

    private static final ModuleCode ODONTOGRAM = new ModuleCode("odontogram");

    private static final ModuleCode BODY_MAP = new ModuleCode("bodymap");

    @Autowired
    private ModuleActivationService modules;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-COMPONENT");
    }

    @Test
    void publishingAnOdontogramWithoutItsModuleIsRefused() {
        Long draftId = templates.draft("Dental chart", null, odontogram()).id();

        assertThatThrownBy(() -> templates.publish(draftId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("module odontogram is not active in this clinic");
    }

    @Test
    void theOdontogramRendersEveryPermanentToothOnceItsModuleIsActive() {
        modules.activate(ODONTOGRAM);
        Long id = openWith(odontogram());

        SheetField chart = onlyFieldOf(id);

        assertThat(chart.fieldType()).isEqualTo("ODONTOGRAM");
        assertThat(chart.options()).hasSize(32).startsWith("11", "12").endsWith("47", "48");
    }

    @Test
    void aToothOutsideTheChartRefusesTheCompletion() {
        modules.activate(ODONTOGRAM);
        Long id = openWith(odontogram());
        encounters.fill(id, RecordValues.of(Map.of("chart", Map.of("99", "carie"))));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart (Dental chart) does not know the region 99");
    }

    @Test
    void aMarkedToothIsAccepted() {
        modules.activate(ODONTOGRAM);
        Long id = openWith(odontogram());
        encounters.fill(id, RecordValues.of(Map.of("chart", Map.of("26", "carie"))));

        assertThat(completeAsPractitioner(id).isCompleted()).isTrue();
    }

    @Test
    void theBodyMapRendersTheRegionsDeclaredInTheTemplate() {
        modules.activate(BODY_MAP);
        Long id = openWith(bodyMap());

        SheetField chart = onlyFieldOf(id);

        assertThat(chart.fieldType()).isEqualTo("BODY_MAP");
        assertThat(chart.options()).containsExactly("ombro", "joelho");
    }

    @Test
    void theComponentLeavesNoTraceOnceItsModuleIsSwitchedOff() {
        modules.activate(ODONTOGRAM);
        Long id = openWith(odontogram());
        modules.deactivate(ODONTOGRAM);

        assertThat(onlySectionOf(id)).isEmpty();
    }

    private Long openWith(TemplateContent content) {
        Long templateId = publishTemplate("Dental chart", content);
        return encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();
    }

    private TemplateContent odontogram() {
        return componentNamed("ODONTOGRAM", "odontogram", List.of());
    }

    private TemplateContent bodyMap() {
        return componentNamed("BODY_MAP", "bodymap", List.of("ombro", "joelho"));
    }

    private TemplateContent componentNamed(String component, String module, List<String> options) {
        return new TemplateContent(List.of(new SectionContent(
                "Exam",
                List.of(new FieldContent(
                        "chart", "Dental chart", "COMPONENT", component, false, options, Map.of(), module)))));
    }

    private SheetField onlyFieldOf(Long encounterId) {
        return onlySectionOf(encounterId).getFirst();
    }

    private List<SheetField> onlySectionOf(Long encounterId) {
        EncounterSnapshot encounter = reopen(encounterId);
        return encounter.sheet().sections().getFirst().fields();
    }
}
