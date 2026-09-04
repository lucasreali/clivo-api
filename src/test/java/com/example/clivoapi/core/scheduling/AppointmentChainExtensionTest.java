package com.example.clivoapi.core.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.AppointmentValidator;
import com.example.clivoapi.patterns.chain.OverlapValidator;
import com.example.clivoapi.patterns.chain.ScheduleBlockValidator;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

class AppointmentChainExtensionTest extends SchedulingFixture {

    private static final int BETWEEN_BLOCK_AND_OVERLAP =
            (ScheduleBlockValidator.ORDER + OverlapValidator.ORDER) / 2;

    private static final LocalTime CLOSED_FROM = LocalTime.of(15, 0);

    private static final String REFUSAL = "the afternoon is closed for maintenance";

    @TestConfiguration
    static class AfternoonMaintenance {

        @Bean
        @Order(BETWEEN_BLOCK_AND_OVERLAP)
        AppointmentValidator afternoonMaintenanceValidator() {
            return proposal -> {
                if (proposal.period().startTime().isBefore(CLOSED_FROM)) {
                    return;
                }
                throw new BusinessException(REFUSAL);
            };
        }
    }

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-CHAIN-EXT");
    }

    @Test
    void aValidatorDeclaredOutsideTheProductionCodeJoinsTheChain() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(nextWeekAt(DayOfWeek.MONDAY, "15:00")))
                .withMessage(REFUSAL);
    }

    @Test
    void theRestOfTheChainKeepsWorkingAroundTheNewValidator() {
        assertThat(bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).status())
                .isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void theNewValidatorRunsAfterTheScheduleBlockOne() {
        LocalDateTime afternoon = nextWeekAt(DayOfWeek.MONDAY, "15:00");
        blocks.register(new ScheduleBlockDetails(null, windowOf(afternoon, 60), new BlockReason("Feriado")));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(afternoon))
                .withMessageContaining("the agenda is blocked");
    }

    @Test
    void theNewValidatorRunsBeforeTheOverlapOne() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.TUESDAY, "14:30");
        bookAt(morning);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(nextWeekAt(DayOfWeek.TUESDAY, "15:00")))
                .withMessage(REFUSAL);
    }
}
