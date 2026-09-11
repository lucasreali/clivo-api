package com.example.clivoapi.patterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.common.extension.BatchCandidates;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ExpiredBatchPolicy;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.EncounterSnapshot;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.AppointmentStatus;
import com.example.clivoapi.core.scheduling.BlockReason;
import com.example.clivoapi.core.scheduling.ScheduleBlockDetails;
import com.example.clivoapi.core.scheduling.ScheduleBlockService;
import com.example.clivoapi.modules.batch.BatchCode;
import com.example.clivoapi.modules.batch.BatchDetails;
import com.example.clivoapi.modules.batch.BatchService;
import com.example.clivoapi.modules.inventory.InventoryService;
import com.example.clivoapi.modules.inventory.MeasurementUnit;
import com.example.clivoapi.modules.inventory.ProductDetails;
import com.example.clivoapi.modules.inventory.Quantity;
import com.example.clivoapi.patterns.factory.Field;
import com.example.clivoapi.patterns.factory.FieldDefinition;
import com.example.clivoapi.patterns.factory.FieldFactory;
import com.example.clivoapi.support.Clinic;
import com.example.clivoapi.support.ClinicFixture;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

class ReuseTest extends ClinicFixture {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private static final ModuleCode BATCH = new ModuleCode("batch");

    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");

    private static final String QUARANTINE = "quarantine";

    private static final String QUARANTINED = "every batch of Anaesthetic is under quarantine";

    private static final int AFTER_THE_SCHEDULE_BLOCK = 25;

    private static final LocalTime STAFF_MEETING = LocalTime.of(15, 0);

    private static final String MEETING = "the whole staff is in the weekly meeting";

    private static final String EMOJI_SCALE = "EMOJI_SCALE";

    @TestConfiguration
    static class ClinicWrittenExtensions {

        @Bean(QUARANTINE)
        ExpiredBatchPolicy quarantineExpiredBatches() {
            return candidates -> candidates.firstFresh()
                    .map(BatchChoice::accepted)
                    .orElseThrow(() -> quarantineOf(candidates));
        }

        @Bean
        @Order(AFTER_THE_SCHEDULE_BLOCK)
        AppointmentValidator staffMeetingValidator() {
            return proposal -> {
                if (proposal.period().startTime().equals(STAFF_MEETING)) {
                    throw new BusinessException(MEETING);
                }
            };
        }

        @Bean(EMOJI_SCALE)
        FieldFactory emojiScale() {
            return definition -> new Field() {

                @Override
                public SheetField fill(RecordValues values) {
                    return definition.renderedWith(values);
                }

                @Override
                public void check(RecordValues values) {
                    definition.requirePresenceIn(values);
                }
            };
        }

        private static BusinessException quarantineOf(BatchCandidates candidates) {
            return new BusinessException("every batch of %s is under quarantine".formatted(candidates.productName()));
        }
    }

    @Autowired
    private InventoryService inventory;

    @Autowired
    private BatchService batches;

    @Autowired
    private ScheduleBlockService blocks;

    @AfterEach
    void narrowTheParameterCatalogueBack() {
        jdbcTemplate.update(
                "UPDATE parameter SET data_type = 'BOOLEAN', options = NULL WHERE code = 'block_expired_batch'");
    }

    @Test
    void aPolicyWrittenOutsideProductionCodeIsResolvedByTheService() {
        openClinic("TEST-REUSE-STRATEGY");
        activate(INVENTORY);
        activate(BATCH);
        acceptQuarantineInTheParameterCatalogue();
        change(BLOCK_EXPIRED_BATCH, QUARANTINE);
        UUID anaesthetic = aBatchControlledProduct();

        receiveBatch(anaesthetic, "L-OLD", LocalDate.now().minusDays(2));
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> batches.selectFor(anaesthetic, Quantity.of("1")))
                .withMessage(QUARANTINED);

        receiveBatch(anaesthetic, "L-NEW", LocalDate.now().plusMonths(8));
        assertThat(batches.selectFor(anaesthetic, Quantity.of("1")).code()).isEqualTo("L-NEW");
    }

    @Test
    void aValidatorWrittenOutsideProductionCodeJoinsTheChainAtItsDeclaredPosition() {
        Clinic clinic = openClinic("TEST-REUSE-CHAIN");

        assertThat(bookAt(clinic, nextWeekAt(DayOfWeek.MONDAY, "09:00")).status())
                .isEqualTo(AppointmentStatus.SCHEDULED);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, nextWeekAt(DayOfWeek.MONDAY, "15:00")))
                .withMessage(MEETING);

        bookAt(clinic, nextWeekAt(DayOfWeek.TUESDAY, "14:30"));
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, nextWeekAt(DayOfWeek.TUESDAY, "15:00")))
                .withMessage(MEETING);

        blockTheAgendaAt(clinic, nextWeekAt(DayOfWeek.WEDNESDAY, "15:00"));
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, nextWeekAt(DayOfWeek.WEDNESDAY, "15:00")))
                .withMessageContaining("the agenda is blocked");
    }

    @Test
    void aFieldTypeWrittenOutsideProductionCodeIsRenderedInTheSheet() {
        Clinic clinic = openClinic("TEST-REUSE-FACTORY");
        UUID templateId = publishTemplate("Wellbeing", sectionWith("Wellbeing", fieldOf("mood", EMOJI_SCALE)));
        UUID encounterId = openEncounter(clinic, templateId);

        encounters.fill(encounterId, RecordValues.of(Map.of("mood", "great")));

        SheetField mood = onlyFieldOf(encounters.findOne(encounterId, Role.PRACTITIONER));
        assertThat(mood.fieldType()).isEqualTo(EMOJI_SCALE);
        assertThat(mood.value()).isEqualTo("great");
    }

    private void acceptQuarantineInTheParameterCatalogue() {
        jdbcTemplate.update(
                "UPDATE parameter SET data_type = 'ENUM', options = ?::jsonb WHERE code = 'block_expired_batch'",
                "[\"true\",\"false\",\"%s\"]".formatted(QUARANTINE));
    }

    private void blockTheAgendaAt(Clinic clinic, LocalDateTime moment) {
        blocks.register(new ScheduleBlockDetails(
                clinic.practitionerId(),
                TimeWindow.of(moment, moment.plusMinutes(SERVICE_MINUTES)),
                new BlockReason("Reforma da sala")));
    }

    private UUID aBatchControlledProduct() {
        return inventory
                .register(new ProductDetails("Anaesthetic", new MeasurementUnit("ml"), Quantity.none(), true))
                .id();
    }

    private void receiveBatch(UUID productId, String code, LocalDate expiresOn) {
        batches.receive(productId, new BatchDetails(new BatchCode(code), expiresOn, Quantity.of("10"), "Lab Alpha"));
    }

    private SheetField onlyFieldOf(EncounterSnapshot encounter) {
        return encounter.sheet().sections().getFirst().fields().getFirst();
    }
}
