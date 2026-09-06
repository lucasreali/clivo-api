package com.example.clivoapi.scenarios;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.common.extension.SheetSection;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.example.clivoapi.support.Clinic;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecordScenarios extends ScenarioTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private static final String MICROCHIP = "microchip";

    private static final String SUPPLY_USED = "supply_used";

    @Test
    void ct10_aBrandNewTemplateIsRenderedWithoutTouchingTheSchema() {
        Clinic clinic = openClinic("TEST-CT10");
        Long templateId = publishTemplate(
                "Veterinary anamnesis", sectionWith("Identification", fieldOf(MICROCHIP, "SHORT_TEXT")));
        Long encounterId = openEncounter(clinic, templateId);

        encounters.fill(encounterId, RecordValues.of(Map.of(MICROCHIP, "981020000123456")));

        assertThat(codesOf(reopen(encounterId))).containsExactly(MICROCHIP);
        assertThat(storedFieldValuesOf(encounterId)).contains(MICROCHIP);
    }

    @Test
    void ct11_anEncounterKeepsTheTemplateVersionItWasFilledIn() {
        Clinic clinic = openClinic("TEST-CT11");
        Long firstVersion = publishTemplate("Anamnesis", sectionWith("Complaint", fieldOf(COMPLAINT, "LONG_TEXT")));
        Long encounterId = openEncounter(clinic, firstVersion);

        Long secondVersion = templates
                .publish(templates
                        .redefine(firstVersion, sectionWith("Complaint", fieldOf(MICROCHIP, "SHORT_TEXT")))
                        .id())
                .id();

        assertThat(secondVersion).isNotEqualTo(firstVersion);
        assertThat(reopen(encounterId).sheet().templateVersion()).isEqualTo(1);
        assertThat(codesOf(reopen(encounterId))).containsExactly(COMPLAINT);
        assertThat(templates.findOne(secondVersion).version()).isEqualTo((short) 2);
    }

    @Test
    void ct12_aFieldOfAnInactiveModuleLeavesNoTraceInTheSheet() {
        Clinic clinic = openClinic("TEST-CT12");
        activate(INVENTORY);
        Long templateId = publishTemplate(
                "Dressing",
                sectionWith("Care", fieldOf(COMPLAINT, "LONG_TEXT"), inventoryField(SUPPLY_USED)));
        Long encounterId = openEncounter(clinic, templateId);

        assertThat(codesOf(reopen(encounterId))).containsExactly(COMPLAINT, SUPPLY_USED);

        modules.deactivate(INVENTORY);
        assertThat(codesOf(reopen(encounterId))).containsExactly(COMPLAINT);
    }

    private FieldContent inventoryField(String code) {
        return new FieldContent(code, code, "SHORT_TEXT", null, false, List.of(), Map.of(), INVENTORY.value());
    }

    private EncounterSnapshot reopen(Long encounterId) {
        return encounters.findOne(encounterId, Role.PRACTITIONER);
    }

    private List<String> codesOf(EncounterSnapshot encounter) {
        return encounter.sheet().sections().stream()
                .map(SheetSection::fields)
                .flatMap(List::stream)
                .map(SheetField::code)
                .toList();
    }

    private String storedFieldValuesOf(Long encounterId) {
        return jdbcTemplate.queryForObject(
                "SELECT field_values::text FROM encounter WHERE id = ?", String.class, encounterId);
    }
}
