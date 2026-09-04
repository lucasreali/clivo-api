package com.example.clivoapi.configuration.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecordTemplateServiceTest extends DatabaseTest {

    @Autowired
    private RecordTemplateService templates;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void draftIsPublishedWithItsSectionsAndFields() {
        Tenant clinic = createTenant("TEST-PUBLISH");
        TemplateSnapshot draft = draftIn(clinic, "Anamnesis", "weight");

        TemplateSnapshot published = valueInTenant(clinic, () -> templates.publish(draft.id()));

        assertThat(published.status()).isEqualTo(RecordTemplateStatus.PUBLISHED);
        assertThat(published.version()).isEqualTo((short) 1);
        assertThat(published.content().sections())
                .singleElement()
                .extracting(SectionContent::name)
                .isEqualTo("Complaint");
        assertThat(fieldCodesOf(published)).containsExactly("weight");
    }

    @Test
    void changingAPublishedTemplateOpensVersionTwoAndKeepsVersionOne() {
        Tenant clinic = createTenant("TEST-VERSION");
        TemplateSnapshot first = publishedIn(clinic, "Anamnesis", "weight");

        TemplateSnapshot second =
                valueInTenant(clinic, () -> templates.redefine(first.id(), contentWithField("temperature")));

        assertThat(second.version()).isEqualTo((short) 2);
        assertThat(second.status()).isEqualTo(RecordTemplateStatus.DRAFT);
        assertThat(fieldCodesOf(second)).containsExactly("temperature");
        assertThat(fieldCodesOf(valueInTenant(clinic, () -> templates.findOne(first.id())))).containsExactly("weight");
    }

    @Test
    void publishingTheNewVersionRetiresThePreviousOne() {
        Tenant clinic = createTenant("TEST-TEMPLATE-RETIRE");
        TemplateSnapshot first = publishedIn(clinic, "Anamnesis", "weight");
        TemplateSnapshot second =
                valueInTenant(clinic, () -> templates.redefine(first.id(), contentWithField("temperature")));

        valueInTenant(clinic, () -> templates.publish(second.id()));

        assertThat(valueInTenant(clinic, () -> templates.findOne(first.id()).status()))
                .isEqualTo(RecordTemplateStatus.RETIRED);
    }

    @Test
    void publishingATemplateOfAnInactiveModuleIsRefused() {
        Tenant clinic = createTenant("TEST-TEMPLATE-MODULE");
        TemplateSnapshot draft = valueInTenant(
                clinic, () -> templates.draft("Stock check", "inventory", contentWithField("batchCode")));

        assertThatThrownBy(() -> valueInTenant(clinic, () -> templates.publish(draft.id())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("module inventory is not active in this clinic");
    }

    @Test
    void publishingATemplateOfAnActiveModuleIsAccepted() {
        Tenant clinic = createTenant("TEST-TEMPLATE-ACTIVE");
        inTenant(clinic, () -> modules.activate(new ModuleCode("inventory")));
        TemplateSnapshot draft = valueInTenant(
                clinic, () -> templates.draft("Stock check", "inventory", contentWithField("batchCode")));

        TemplateSnapshot published = valueInTenant(clinic, () -> templates.publish(draft.id()));

        assertThat(published.status()).isEqualTo(RecordTemplateStatus.PUBLISHED);
    }

    @Test
    void componentFieldWithoutAComponentNameIsRefused() {
        Tenant clinic = createTenant("TEST-COMPONENT");
        TemplateContent content = new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent("chart", "Chart", "COMPONENT", null, false, null, null, null)))));

        assertThatThrownBy(() -> valueInTenant(clinic, () -> templates.draft("Chart", null, content)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field chart must name a component when its type is COMPONENT, and none otherwise");
    }

    @Test
    void aFieldTypeOutsideTheKnownListIsStoredAsGiven() {
        Tenant clinic = createTenant("TEST-NEW-TYPE");
        TemplateContent content = new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent("mood", "Mood", "EMOJI_SCALE", null, false, null, null, null)))));

        TemplateSnapshot draft = valueInTenant(clinic, () -> templates.draft("Wellbeing", null, content));

        assertThat(fieldsOf(draft)).singleElement().extracting(FieldContent::fieldType).isEqualTo("EMOJI_SCALE");
    }

    @Test
    void templateOfOneClinicDoesNotReachTheOther() {
        Tenant first = createTenant("TEST-TEMPLATE-FIRST");
        Tenant second = createTenant("TEST-TEMPLATE-SECOND");
        draftIn(first, "Anamnesis", "weight");

        assertThat(valueInTenant(second, templates::findAll)).isEmpty();
        assertThat(valueInTenant(first, templates::findAll)).hasSize(1);
    }

    private TemplateSnapshot draftIn(Tenant clinic, String name, String fieldCode) {
        return valueInTenant(clinic, () -> templates.draft(name, null, contentWithField(fieldCode)));
    }

    private TemplateSnapshot publishedIn(Tenant clinic, String name, String fieldCode) {
        TemplateSnapshot draft = draftIn(clinic, name, fieldCode);
        return valueInTenant(clinic, () -> templates.publish(draft.id()));
    }

    private TemplateContent contentWithField(String fieldCode) {
        return new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent(
                        fieldCode, fieldCode, "SHORT_TEXT", null, true, List.of(), Map.of("maxLength", 40), null)))));
    }

    private List<FieldContent> fieldsOf(TemplateSnapshot snapshot) {
        return snapshot.content().sections().stream().flatMap(section -> section.fields().stream()).toList();
    }

    private List<String> fieldCodesOf(TemplateSnapshot snapshot) {
        return fieldsOf(snapshot).stream().map(FieldContent::code).toList();
    }
}
