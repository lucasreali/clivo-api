package com.example.clivoapi.modules.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.core.practitioner.AvailabilityPeriod;
import com.example.clivoapi.core.practitioner.PractitionerDetails;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.core.practitioner.TimeRange;
import com.example.clivoapi.core.practitioner.Weekday;
import com.example.clivoapi.core.practitioner.WeeklySchedule;
import com.example.clivoapi.core.scheduling.AppointmentBooking;
import com.example.clivoapi.core.scheduling.SchedulingFixture;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationServiceTest extends SchedulingFixture {

    private static final ModuleCode NOTIFICATION = new ModuleCode("notification");
    private static final ParameterCode REMINDER_LEAD_HOURS = new ParameterCode("reminder_lead_hours");
    private static final Recipient PHONE = new Recipient("41999990000");

    @Autowired
    private NotificationService notifications;

    @Autowired
    private PractitionerService practitioners;

    @Autowired
    private ModuleActivationService modules;

    @Autowired
    private ClinicParameterService parameters;

    private Long alwaysAvailable;

    @Test
    void aScheduledReminderStartsPendingDelivery() {
        openClinicWithNotifications("TEST-NOTIF-NEW", "72");

        NotificationSnapshot reminder = remindAbout(inDays(1));

        assertThat(reminder.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(reminder.delivery()).isEmpty();
    }

    @Test
    void onlyTheRemindersWithinTheClinicLeadTimeGoOut() {
        openClinicWithNotifications("TEST-NOTIF-LEAD", "72");
        NotificationSnapshot soon = remindAbout(inDays(1));
        NotificationSnapshot later = remindAbout(inDays(10));

        List<NotificationSnapshot> sent = notifications.dispatchDue();

        assertThat(sent).extracting(NotificationSnapshot::id).containsExactly(soon.id());
        assertThat(notifications.findOne(soon.id()).status()).isEqualTo(NotificationStatus.SENT);
        assertThat(notifications.findOne(soon.id()).delivery()).isPresent();
        assertThat(notifications.findOne(later.id()).status()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void aShorterLeadTimeHoldsBackTheSameReminder() {
        openClinicWithNotifications("TEST-NOTIF-SHORT", "1");
        remindAbout(inDays(1));

        assertThat(notifications.dispatchDue()).isEmpty();

        parameters.change(REMINDER_LEAD_HOURS, ParameterValue.of("72"));

        assertThat(notifications.dispatchDue()).hasSize(1);
    }

    @Test
    void theAnswerOfTheCustomerIsRegisteredOnTheReminderThatWasSent() {
        openClinicWithNotifications("TEST-NOTIF-REPLY", "72");
        NotificationSnapshot reminder = remindAbout(inDays(1));
        notifications.dispatchDue();

        NotificationSnapshot answered = notifications.registerResponse(reminder.id(), "CONFIRMO");

        assertThat(answered.status()).isEqualTo(NotificationStatus.REPLIED);
        assertThat(answered.answer()).contains("CONFIRMO");
    }

    @Test
    void aReminderThatNeverWentOutHasNoAnswerToRegister() {
        openClinicWithNotifications("TEST-NOTIF-EARLY", "72");
        NotificationSnapshot reminder = remindAbout(inDays(10));

        assertThatThrownBy(() -> notifications.registerResponse(reminder.id(), "CONFIRMO"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a reminder in status PENDING received no answer to register");
    }

    @Test
    void anAnswerLongerThanTheChannelAllowsIsRefused() {
        assertThatThrownBy(() -> new Reply("confirmo o meu atendimento de amanha"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a reply is limited to 20 characters");
    }

    private void openClinicWithNotifications(String code, String leadHours) {
        openClinic(code);
        modules.activate(NOTIFICATION);
        parameters.change(REMINDER_LEAD_HOURS, ParameterValue.of(leadHours));
        alwaysAvailable = practitioners.register(new PractitionerDetails("Dr. Plantão", null)).id();
        practitioners.follow(alwaysAvailable, everyDay());
    }

    private NotificationSnapshot remindAbout(LocalDateTime start) {
        Long appointmentId = scheduling
                .schedule(new AppointmentBooking(customerId(), alwaysAvailable, serviceId(), start))
                .id();
        return notifications.schedule(appointmentId, NotificationChannel.WHATSAPP, PHONE);
    }

    private LocalDateTime inDays(int days) {
        return LocalDate.now().plusDays(days).atTime(LocalTime.of(9, 0));
    }

    private WeeklySchedule everyDay() {
        TimeRange hours = new TimeRange(LocalTime.of(7, 0), LocalTime.of(20, 0));
        return new WeeklySchedule(Arrays.stream(DayOfWeek.values())
                .map(day -> new AvailabilityPeriod(Weekday.of(day), hours))
                .toList());
    }
}
