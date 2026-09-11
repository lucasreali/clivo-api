package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.extension.MarkedRegionState;
import com.example.clivoapi.common.extension.MarkingSource;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.support.Clinic;
import com.example.clivoapi.support.ClinicFixture;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MarkingProvenanceTest extends ClinicFixture {

    private static final ModuleCode ODONTOGRAM = new ModuleCode("odontogram");

    private static final String CHART = "chart";

    private static final String LABEL = "Dental chart";

    @Test
    void whatThisSessionMarkedIsToldApartFromWhatCameBefore() {
        Clinic clinic = openDentalClinic("TEST-PROV-SOURCE");
        UUID template = dentalTemplate();
        completedEncounter(clinic, template, marking("26", "occlusal", "caries"));
        UUID open = openEncounter(clinic, template);

        encounters.fill(open, valuesOf(marking("36", "occlusal", "caries")));

        assertThat(markingsOf(open))
                .extracting(MarkedRegionState::region, state -> state.origin().source())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("26", MarkingSource.HISTORY),
                        org.assertj.core.groups.Tuple.tuple("36", MarkingSource.SESSION));
    }

    @Test
    void aConditionCarriesTheDateItFirstAppeared() {
        Clinic clinic = openDentalClinic("TEST-PROV-SINCE");
        UUID template = dentalTemplate();
        UUID first = completedEncounter(clinic, template, marking("11", "vestibular", "restored"));
        completedEncounter(clinic, template, marking("11", "vestibular", "restored"));
        UUID open = openEncounter(clinic, template);

        MarkedRegionState carried = onlyMarkingOf(open);

        assertThat(carried.origin().since()).isEqualTo(recordedAtOf(first));
        assertThat(carried.origin().recordedAt()).isAfter(carried.origin().since());
    }

    @Test
    void aChangedConditionRestartsTheDateItAppeared() {
        Clinic clinic = openDentalClinic("TEST-PROV-CHANGE");
        UUID template = dentalTemplate();
        UUID first = completedEncounter(clinic, template, marking("11", "vestibular", "caries"));
        UUID second = completedEncounter(clinic, template, marking("11", "vestibular", "restored"));
        UUID open = openEncounter(clinic, template);

        MarkedRegionState carried = onlyMarkingOf(open);

        assertThat(carried.mark()).isEqualTo("restored");
        assertThat(carried.origin().since()).isEqualTo(recordedAtOf(second));
        assertThat(recordedAtOf(first)).isBefore(carried.origin().since());
    }

    @Test
    void theOpenSessionOverwritesWhatAnEarlierEncounterMarkedOnTheSameFace() {
        Clinic clinic = openDentalClinic("TEST-PROV-WINS");
        UUID template = dentalTemplate();
        completedEncounter(clinic, template, marking("11", "vestibular", "caries"));
        UUID open = openEncounter(clinic, template);

        encounters.fill(open, valuesOf(marking("11", "vestibular", "restored")));

        MarkedRegionState resolved = onlyMarkingOf(open);
        assertThat(resolved.mark()).isEqualTo("restored");
        assertThat(resolved.origin().source()).isEqualTo(MarkingSource.SESSION);
    }

    @Test
    void theComparisonReportsWhatChangedSinceAnEarlierInstant() {
        Clinic clinic = openDentalClinic("TEST-PROV-DIFF");
        UUID template = dentalTemplate();
        UUID first = completedEncounter(clinic, template, marking("11", "vestibular", "caries"));
        Instant afterTheFirst = recordedAtOf(first);
        completedEncounter(clinic, template, marking("11", "vestibular", "restored"));
        UUID open = openEncounter(clinic, template);
        encounters.fill(open, valuesOf(marking("11", "vestibular", "restored"), marking("21", "incisal", "caries")));

        RecordComparison comparison = encounters.compare(open, afterTheFirst, Role.PRACTITIONER);

        assertThat(comparison.asOf()).isEqualTo(afterTheFirst);
        assertThat(changesOf(comparison))
                .extracting(MarkingChange::region, MarkingChange::change)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("11", ChangeKind.CHANGED),
                        org.assertj.core.groups.Tuple.tuple("21", ChangeKind.ADDED));
    }

    @Test
    void aFaceUntouchedSinceTheBaselineIsNotReportedAsAChange() {
        Clinic clinic = openDentalClinic("TEST-PROV-SAME");
        UUID template = dentalTemplate();
        UUID first = completedEncounter(clinic, template, marking("11", "vestibular", "caries"));
        UUID open = openEncounter(clinic, template);

        RecordComparison comparison = encounters.compare(open, recordedAtOf(first), Role.PRACTITIONER);

        assertThat(changesOf(comparison)).isEmpty();
    }

    private List<MarkingChange> changesOf(RecordComparison comparison) {
        return comparison.fields().stream().flatMap(field -> field.changes().stream()).toList();
    }

    private MarkedRegionState onlyMarkingOf(UUID encounterId) {
        return markingsOf(encounterId).getFirst();
    }

    private List<MarkedRegionState> markingsOf(UUID encounterId) {
        return chartOf(encounterId).markings();
    }

    private SheetField chartOf(UUID encounterId) {
        return encounters
                .findOne(encounterId, Role.PRACTITIONER)
                .sheet()
                .sections()
                .getFirst()
                .fields()
                .getFirst();
    }

    private Instant recordedAtOf(UUID encounterId) {
        return encounters.findOne(encounterId, Role.PRACTITIONER).timing().completedAt();
    }

    private UUID completedEncounter(Clinic clinic, UUID template, Map<String, Object> marking) {
        UUID id = openEncounter(clinic, template);
        encounters.fill(id, valuesOf(marking));
        encounters.complete(id, Role.PRACTITIONER);
        return id;
    }

    private Clinic openDentalClinic(String name) {
        Clinic clinic = openClinic(name);
        activate(ODONTOGRAM);
        return clinic;
    }

    private UUID dentalTemplate() {
        return publishTemplate(
                LABEL,
                new TemplateContent(List.of(new SectionContent(
                        "Exam",
                        List.of(new FieldContent(
                                CHART, LABEL, "COMPONENT", "ODONTOGRAM", false, List.of(), Map.of(),
                                ODONTOGRAM.value()))))));
    }

    private RecordValues valuesOf(Map<String, Object>... markings) {
        return RecordValues.of(Map.of(CHART, List.of(markings)));
    }

    private Map<String, Object> marking(String region, String part, String mark) {
        return Map.of("region", region, "parts", List.of(part), "mark", mark);
    }
}
