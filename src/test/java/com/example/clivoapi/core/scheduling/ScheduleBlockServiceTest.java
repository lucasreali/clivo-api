package com.example.clivoapi.core.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.time.TimeWindow;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleBlockServiceTest extends SchedulingFixture {

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-BLOCK");
    }

    @Test
    void aBlockIsRegisteredWithItsReasonAndFoundByPeriod() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "08:00");
        blocks.register(new ScheduleBlockDetails(practitionerId(), windowOf(morning, 240), new BlockReason("Congresso")));

        List<ScheduleBlockSnapshot> registered = blocks.findWithin(windowOf(morning, 600));

        assertThat(registered).singleElement()
                .satisfies(block -> assertThat(block.reason()).isEqualTo("Congresso"))
                .satisfies(block -> assertThat(block.practitioner()).contains(practitionerId()));
    }

    @Test
    void aBlockOutsideTheQueriedPeriodIsNotListed() {
        LocalDateTime monday = nextWeekAt(DayOfWeek.MONDAY, "08:00");
        blocks.register(new ScheduleBlockDetails(null, windowOf(monday, 120), new BlockReason("Manutencao")));

        assertThat(blocks.findWithin(windowOf(monday.plusDays(1), 120))).isEmpty();
    }

    @Test
    void aBlockWithoutPractitionerReachesEveryoneInTheClinic() {
        LocalDateTime holiday = nextWeekAt(DayOfWeek.TUESDAY, "08:00");
        Long otherPractitioner = registerPractitioner("Dr. Bruno");
        blocks.register(new ScheduleBlockDetails(null, windowOf(holiday, 600), new BlockReason("Feriado")));

        assertThat(blocks.firstBlocking(practitionerId(), windowOf(holiday, 60))).isPresent();
        assertThat(blocks.firstBlocking(otherPractitioner, windowOf(holiday, 60))).isPresent();
    }

    @Test
    void aBlockOfOnePractitionerLeavesTheOtherFree() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.TUESDAY, "09:00");
        Long otherPractitioner = registerPractitioner("Dr. Bruno");
        blocks.register(new ScheduleBlockDetails(practitionerId(), windowOf(morning, 60), new BlockReason("Ferias")));

        assertThat(blocks.firstBlocking(practitionerId(), windowOf(morning, 30))).isPresent();
        assertThat(blocks.firstBlocking(otherPractitioner, windowOf(morning, 30))).isEmpty();
    }

    @Test
    void aBlockOnlyCoversTheMomentsInsideItsPeriod() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        TimeWindow period = windowOf(morning, 60);
        blocks.register(new ScheduleBlockDetails(practitionerId(), period, new BlockReason("Reuniao")));

        assertThat(blocks.firstBlocking(practitionerId(), windowOf(morning.plusMinutes(30), 30))).isPresent();
        assertThat(blocks.firstBlocking(practitionerId(), windowOf(morning.plusHours(2), 30))).isEmpty();
    }

    @Test
    void aBlockCoversOnlyTheMomentsBetweenItsBounds() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        ScheduleBlock block = new ScheduleBlock(null, windowOf(morning, 60), new BlockReason("Reuniao"));

        assertThat(block.covers(morning)).isTrue();
        assertThat(block.covers(morning.plusMinutes(59))).isTrue();
        assertThat(block.covers(morning.plusMinutes(60))).isFalse();
        assertThat(block.isClinicWide()).isTrue();
    }

    @Test
    void aBlockWithoutReasonIsRefused() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> new ScheduleBlockDetails(null, windowOf(morning, 60), new BlockReason(" ")))
                .withMessage("a reason is required to block the agenda");
    }

    @Test
    void aBlockIsReleasedAndStopsBeingListed() {
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        Long id = blocks.register(
                new ScheduleBlockDetails(null, windowOf(morning, 60), new BlockReason("Reuniao"))).id();

        blocks.release(id);

        assertThat(blocks.findWithin(windowOf(morning, 600))).isEmpty();
    }
}
