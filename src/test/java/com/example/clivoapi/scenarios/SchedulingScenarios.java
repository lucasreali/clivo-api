package com.example.clivoapi.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.scheduling.AppointmentStatus;
import com.example.clivoapi.core.scheduling.BlockReason;
import com.example.clivoapi.core.scheduling.ScheduleBlockDetails;
import com.example.clivoapi.core.scheduling.ScheduleBlockService;
import com.example.clivoapi.support.Clinic;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SchedulingScenarios extends ScenarioTest {

    private static final ParameterCode RESCHEDULE_WINDOW_HOURS = new ParameterCode("reschedule_window_hours");

    private static final String LONGEST_WINDOW = "72";

    private static final String SHORTEST_WINDOW = "1";

    @Autowired
    private ScheduleBlockService blocks;

    @Test
    void ct06_twoAppointmentsOfTheSamePractitionerCannotShareAnHour() {
        Clinic clinic = openClinic("TEST-CT06");
        LocalDateTime morning = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        bookAt(clinic, morning);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, morning.plusMinutes(30)))
                .withMessageContaining("the practitioner already has an appointment");
    }

    @Test
    void ct07_aBlockedAgendaRefusesTheAppointmentAndSaysWhy() {
        Clinic clinic = openClinic("TEST-CT07");
        LocalDateTime morning = nextWeekAt(DayOfWeek.TUESDAY, "09:00");
        blocks.register(new ScheduleBlockDetails(
                clinic.practitionerId(),
                TimeWindow.of(morning, morning.plusMinutes(SERVICE_MINUTES)),
                new BlockReason("Manutenção do equipamento")));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, morning))
                .withMessageContaining("the agenda is blocked");
    }

    @Test
    void ct08_anHourOutsideTheWeeklyScheduleIsRefused() {
        Clinic clinic = openClinic("TEST-CT08");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> bookAt(clinic, nextWeekAt(DayOfWeek.MONDAY, "19:00")))
                .withMessageContaining("does not attend");
    }

    @Test
    void ct09_theReschedulingDeadlineIsTheOneTheClinicConfigured() {
        Clinic clinic = openClinic("TEST-CT09");
        LocalDateTime tomorrow = tomorrowAt("09:00");
        UUID appointmentId = bookAt(clinic, tomorrow).id();

        change(RESCHEDULE_WINDOW_HOURS, LONGEST_WINDOW);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> scheduling.reschedule(appointmentId, tomorrow.plusHours(1)))
                .withMessage("an appointment can only be rescheduled until 72 hours before it starts");

        change(RESCHEDULE_WINDOW_HOURS, SHORTEST_WINDOW);
        assertThat(scheduling.reschedule(appointmentId, tomorrow.plusHours(1)).status())
                .isEqualTo(AppointmentStatus.SCHEDULED);
    }

    private LocalDateTime tomorrowAt(String time) {
        return LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.parse(time));
    }
}
