package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.common.extension.SheetSection;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.patterns.factory.Field;
import com.example.clivoapi.patterns.factory.FieldDefinition;
import com.example.clivoapi.patterns.factory.FieldFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

class RecordFactoryExtensionTest extends EncounterFixture {

    private static final String EMOJI_SCALE = "EMOJI_SCALE";

    private static final List<String> FACES = List.of("bad", "fine", "great");

    @TestConfiguration
    static class MoodFieldType {

        @Bean(EMOJI_SCALE)
        FieldFactory emojiScale() {
            return MoodField::new;
        }
    }

    static final class MoodField implements Field {

        private final FieldDefinition definition;

        MoodField(FieldDefinition definition) {
            this.definition = definition;
        }

        @Override
        public SheetField fill(RecordValues values) {
            return definition.renderedWith(values);
        }

        @Override
        public void check(RecordValues values) {
            definition.requirePresenceIn(values);
            definition.valueIn(values).ifPresent(this::checkFace);
        }

        private void checkFace(Object value) {
            if (FACES.contains(value.toString())) {
                return;
            }
            throw definition.refusal("expects one of %s".formatted(FACES));
        }
    }

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-FIELD-EXT");
    }

    @Test
    void aFieldTypeDeclaredOutsideTheProductionCodeIsRendered() {
        Long id = openWithMood();

        SheetField mood = onlyFieldOf(reopen(id));

        assertThat(mood.code()).isEqualTo("mood");
        assertThat(mood.fieldType()).isEqualTo(EMOJI_SCALE);
    }

    @Test
    void theNewFieldTypeValidatesTheFilledValue() {
        Long id = openWithMood();
        encounters.fill(id, RecordValues.of(Map.of("mood", "furious")));

        assertThatThrownBy(() -> completeAsPractitioner(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("field mood (mood) expects one of [bad, fine, great]");
    }

    @Test
    void theNewFieldTypeAcceptsAValueItKnows() {
        Long id = openWithMood();
        encounters.fill(id, RecordValues.of(Map.of("mood", "great")));

        assertThat(completeAsPractitioner(id).isCompleted()).isTrue();
    }

    @Test
    void theBuiltInFieldTypesKeepWorkingBesideTheNewOne() {
        Long templateId = publishTemplate("Wellbeing", moodAndNotes());
        Long id = encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();

        encounters.fill(id, RecordValues.of(Map.of("mood", "fine", "notes", "Tudo certo")));

        assertThat(completeAsPractitioner(id).isCompleted()).isTrue();
    }

    private Long openWithMood() {
        Long templateId = publishTemplate("Mood", complaintWith("mood", EMOJI_SCALE));
        return encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();
    }

    private TemplateContent moodAndNotes() {
        return new TemplateContent(List.of(new SectionContent(
                "Wellbeing",
                List.of(
                        new FieldContent("mood", "Mood", EMOJI_SCALE, null, true, List.of(), Map.of(), null),
                        new FieldContent("notes", "Notes", "LONG_TEXT", null, false, List.of(), Map.of(), null)))));
    }

    private SheetField onlyFieldOf(EncounterSnapshot encounter) {
        List<SheetSection> sections = encounter.sheet().sections();
        return sections.getFirst().fields().getFirst();
    }
}
