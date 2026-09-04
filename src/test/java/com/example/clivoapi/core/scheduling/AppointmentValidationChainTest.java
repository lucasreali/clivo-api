package com.example.clivoapi.core.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppointmentValidationChainTest extends SchedulingFixture {

    private static final ParameterCode RESCHEDULE_WINDOW = new ParameterCode("reschedule_window_hours");

    @Autowired
    private ClinicParameterService parameters;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-CHAIN");
    }

    @Test
    void anAppointmentOutsideTheWeeklyScheduleIsRefused() {
        LocalDateTime sunday = nextWeekAt(DayOfWeek.SUNDAY, "09:00");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(sunday))
                .withMessageContaining("does not attend from");
    }

    @Test
    void anAppointmentEndingAfterTheWeeklyScheduleIsRefused() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(nextWeekAt(DayOfWeek.MONDAY, "17:30")))
                .withMessageContaining("does not attend from");
    }

    @Test
    void anAppointmentOverABlockedAgendaIsRefused() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        blocks.register(new ScheduleBlockDetails(practitionerId(), windowOf(start, 120), new BlockReason("Congresso")));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(start))
                .withMessageContaining("the agenda is blocked")
                .withMessageContaining("Congresso");
    }

    @Test
    void anAppointmentOverAnotherOneOfTheSamePractitionerIsRefused() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        bookAt(start);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(start.plusMinutes(30)))
                .withMessageContaining("already has an appointment from");
    }

    @Test
    void reschedulingInsideTheNoticeWindowIsRefused() {
        Long id = bookAt(soonAt(DayOfWeek.MONDAY, "09:00")).id();
        parameters.change(RESCHEDULE_WINDOW, ParameterValue.of("168"));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> scheduling.reschedule(id, soonAt(DayOfWeek.TUESDAY, "09:00")))
                .withMessage("an appointment can only be rescheduled until 168 hours before it starts");
    }

    @Test
    void reschedulingOutsideTheNoticeWindowIsAccepted() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        Long id = bookAt(start).id();
        parameters.change(RESCHEDULE_WINDOW, ParameterValue.of("1"));

        LocalDateTime later = nextWeekAt(DayOfWeek.TUESDAY, "09:00");

        assertThat(scheduling.reschedule(id, later).period().start()).isEqualTo(later);
    }

    @Test
    void theFourRefusalsCarryDistinctMessages() {
        LocalDateTime blocked = soonAt(DayOfWeek.TUESDAY, "09:00");
        blocks.register(new ScheduleBlockDetails(null, windowOf(blocked, 60), new BlockReason("Feriado")));
        LocalDateTime taken = soonAt(DayOfWeek.MONDAY, "09:00");
        Long id = bookAt(taken).id();
        parameters.change(RESCHEDULE_WINDOW, ParameterValue.of("168"));

        List<String> refusals = List.of(
                refusalOf(() -> bookAt(soonAt(DayOfWeek.SUNDAY, "09:00"))),
                refusalOf(() -> bookAt(blocked)),
                refusalOf(() -> bookAt(taken.plusMinutes(30))),
                refusalOf(() -> scheduling.reschedule(id, soonAt(DayOfWeek.WEDNESDAY, "09:00"))));

        assertThat(refusals).hasSize(4).doesNotHaveDuplicates();
    }

    private String refusalOf(Runnable booking) {
        try {
            booking.run();
            throw new AssertionError("the chain accepted an appointment it should refuse");
        } catch (BusinessException refusal) {
            return refusal.getMessage();
        }
    }
}
