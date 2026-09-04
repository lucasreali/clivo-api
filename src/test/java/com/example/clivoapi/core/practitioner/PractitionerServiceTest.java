package com.example.clivoapi.core.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PractitionerServiceTest extends DatabaseTest {

    @Autowired
    private PractitionerService practitioners;

    @BeforeEach
    void bindClinic() {
        bindTenant(createTenant("TEST-PRACT"));
    }

    @Test
    void theWeeklyScheduleAnswersWhetherThePractitionerWorksAtATime() {
        Long id = practitioners.register(new PractitionerDetails("Dr. Marina", "CRO-12345")).id();

        practitioners.follow(id, scheduleOf(
                periodOf(DayOfWeek.MONDAY, "08:00", "12:00"),
                periodOf(DayOfWeek.MONDAY, "13:00", "18:00"),
                periodOf(DayOfWeek.WEDNESDAY, "09:00", "17:00")));

        assertThat(practitioners.worksAt(id, DayOfWeek.MONDAY, LocalTime.of(9, 30))).isTrue();
        assertThat(practitioners.worksAt(id, DayOfWeek.MONDAY, LocalTime.of(12, 30))).isFalse();
        assertThat(practitioners.worksAt(id, DayOfWeek.MONDAY, LocalTime.of(18, 0))).isFalse();
        assertThat(practitioners.worksAt(id, DayOfWeek.TUESDAY, LocalTime.of(9, 30))).isFalse();
    }

    @Test
    void theWeekOrderOfTheScheduleIsKeptWhateverTheOrderItArrivesIn() {
        Long id = practitioners.register(new PractitionerDetails("Dr. Marina", null)).id();

        PractitionerSnapshot saved = practitioners.follow(id, scheduleOf(
                periodOf(DayOfWeek.WEDNESDAY, "09:00", "17:00"),
                periodOf(DayOfWeek.MONDAY, "13:00", "18:00"),
                periodOf(DayOfWeek.MONDAY, "08:00", "12:00")));

        assertThat(saved.schedule().periods()).extracting(AvailabilityPeriod::toString)
                .containsExactly("MONDAY 08:00-12:00", "MONDAY 13:00-18:00", "WEDNESDAY 09:00-17:00");
    }

    @Test
    void overlappingPeriodsOfTheSamePractitionerAreRefused() {
        practitioners.register(new PractitionerDetails("Dr. Marina", null));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> scheduleOf(
                        periodOf(DayOfWeek.MONDAY, "08:00", "12:00"),
                        periodOf(DayOfWeek.MONDAY, "11:00", "15:00")))
                .withMessageContaining("overlaps another period");
    }

    @Test
    void aPeriodThatEndsBeforeItStartsIsRefused() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> periodOf(DayOfWeek.MONDAY, "18:00", "08:00"))
                .withMessageContaining("ends after it starts");
    }

    @Test
    void anInactivePractitionerWorksAtNoTime() {
        Long id = practitioners.register(new PractitionerDetails("Dr. Marina", null)).id();
        practitioners.follow(id, scheduleOf(periodOf(DayOfWeek.MONDAY, "08:00", "12:00")));

        practitioners.deactivate(id);

        assertThat(practitioners.worksAt(id, DayOfWeek.MONDAY, LocalTime.of(9, 30))).isFalse();
        assertThat(practitioners.findOne(id).status()).isEqualTo(PractitionerStatus.INACTIVE);
    }

    private WeeklySchedule scheduleOf(AvailabilityPeriod... periods) {
        return new WeeklySchedule(List.of(periods));
    }

    private AvailabilityPeriod periodOf(DayOfWeek day, String start, String end) {
        return new AvailabilityPeriod(Weekday.of(day), new TimeRange(LocalTime.parse(start), LocalTime.parse(end)));
    }
}
