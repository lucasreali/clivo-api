package com.example.clivoapi.core.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

class SchedulingServiceTest extends SchedulingFixture {

    @Autowired
    private AppointmentBook book;

    @Autowired
    private AppointmentAssembler assembler;

    @Autowired
    private TransactionTemplate transactions;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-SCHED");
    }

    @Test
    void anAppointmentEndsAfterTheDurationOfItsService() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");

        AppointmentSnapshot booked = bookAt(start);

        assertThat(booked.period().start()).isEqualTo(start);
        assertThat(booked.period().end()).isEqualTo(start.plusMinutes(SERVICE_MINUTES));
        assertThat(booked.status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void theAppointmentCarriesTheNamesOfItsParticipants() {
        AppointmentSnapshot booked = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00"));

        AppointmentParticipants participants = booked.participants();
        assertThat(participants.customerName()).isEqualTo("Ana Prado");
        assertThat(participants.practitionerName()).isEqualTo("Dr. Marina");
        assertThat(participants.serviceName()).isEqualTo("Limpeza");
    }

    @Test
    void reschedulingMovesTheWholePeriod() {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();
        LocalDateTime later = nextWeekAt(DayOfWeek.TUESDAY, "14:00");

        AppointmentSnapshot moved = scheduling.reschedule(id, later);

        assertThat(moved.period().start()).isEqualTo(later);
        assertThat(moved.period().end()).isEqualTo(later.plusMinutes(SERVICE_MINUTES));
    }

    @Test
    void cancellingKeepsTheReasonAndFreesTheSlot() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        UUID id = bookAt(start).id();

        AppointmentSnapshot cancelled = scheduling.cancel(id, new CancellationReason("Cliente desistiu"));

        assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(cancelled.reasonGiven()).contains("Cliente desistiu");
        assertThat(bookAt(start).status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void cancellingWithoutReasonIsRefused() {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> scheduling.cancel(id, new CancellationReason("")))
                .withMessage("a reason is required to close an appointment");
    }

    @Test
    void aCancelledAppointmentIsNotCancelledTwice() {
        UUID id = bookAt(nextWeekAt(DayOfWeek.MONDAY, "09:00")).id();
        scheduling.cancel(id, new CancellationReason("Cliente desistiu"));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> scheduling.cancel(id, new CancellationReason("De novo")))
                .withMessage("an appointment in status CANCELLED cannot be cancelled");
    }

    @Test
    void anUnknownAppointmentIsNotFound() {
        UUID unknown = UUID.randomUUID();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> scheduling.findOne(unknown))
                .withMessage("Appointment %s not found".formatted(unknown));
    }

    @Test
    void anAppointmentOverlapsADraftThatCrossesItsPeriod() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        UUID id = bookAt(start).id();

        transactions.executeWithoutResult(status -> {
            Appointment booked = book.reference(id);
            assertThat(booked.overlaps(draftAt(start.plusMinutes(30)))).isTrue();
            assertThat(booked.overlaps(draftAt(start.plusMinutes(SERVICE_MINUTES)))).isFalse();
        });
    }

    @Test
    void aCancelledAppointmentNoLongerOverlapsAnything() {
        LocalDateTime start = nextWeekAt(DayOfWeek.MONDAY, "09:00");
        UUID id = bookAt(start).id();
        scheduling.cancel(id, new CancellationReason("Cliente desistiu"));

        transactions.executeWithoutResult(status -> {
            Appointment cancelled = book.reference(id);
            assertThat(cancelled.period().overlaps(windowOf(start, SERVICE_MINUTES))).isTrue();
            assertThat(cancelled.overlaps(draftAt(start))).isFalse();
        });
    }

    private Appointment draftAt(LocalDateTime start) {
        return assembler.assemble(new AppointmentBooking(customerId(), practitionerId(), serviceId(), start));
    }
}
