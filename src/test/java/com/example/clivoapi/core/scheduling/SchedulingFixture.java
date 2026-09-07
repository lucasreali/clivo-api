package com.example.clivoapi.core.scheduling;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.time.TimeWindow;
import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.catalog.ServiceDetails;
import com.example.clivoapi.core.catalog.ServiceDuration;
import com.example.clivoapi.core.customer.ContactDetails;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.customer.NationalId;
import com.example.clivoapi.core.customer.PhoneNumber;
import com.example.clivoapi.core.practitioner.AvailabilityPeriod;
import com.example.clivoapi.core.practitioner.PractitionerDetails;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.core.practitioner.TimeRange;
import com.example.clivoapi.core.practitioner.Weekday;
import com.example.clivoapi.core.practitioner.WeeklySchedule;
import com.example.clivoapi.support.GeneratedDocument;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SchedulingFixture extends DatabaseTest {

    protected static final int SERVICE_MINUTES = 60;

    @Autowired
    protected SchedulingService scheduling;

    @Autowired
    protected ScheduleBlockService blocks;

    @Autowired
    private CustomerService customers;

    @Autowired
    private PractitionerService practitioners;

    @Autowired
    private CatalogService catalogue;

    private UUID customerId;
    private UUID practitionerId;
    private UUID serviceId;

    protected void openClinic(String code) {
        bindTenant(createTenant(code));
        customerId = customers.register(customerNamed("Ana Prado", GeneratedDocument.nationalIdTextFor("Ana Prado"))).id();
        practitionerId = registerPractitioner("Dr. Marina");
        serviceId = catalogue.register(
                new ServiceDetails("Limpeza", ServiceDuration.ofMinutes(SERVICE_MINUTES), Money.of("180.00"))).id();
    }

    protected UUID registerPractitioner(String name) {
        UUID id = practitioners.register(new PractitionerDetails(name, null)).id();
        practitioners.follow(id, businessHours());
        return id;
    }

    protected UUID customerId() {
        return customerId;
    }

    protected UUID practitionerId() {
        return practitionerId;
    }

    protected UUID serviceId() {
        return serviceId;
    }

    protected AppointmentSnapshot bookAt(LocalDateTime start) {
        return scheduling.schedule(new AppointmentBooking(customerId, practitionerId, serviceId, start));
    }

    protected LocalDateTime nextWeekAt(DayOfWeek day, String time) {
        LocalDate date = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(day));
        return LocalDateTime.of(date, LocalTime.parse(time));
    }

    protected LocalDateTime soonAt(DayOfWeek day, String time) {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(day)), LocalTime.parse(time));
    }

    protected TimeWindow windowOf(LocalDateTime start, int minutes) {
        return TimeWindow.of(start, start.plusMinutes(minutes));
    }

    private CustomerDetails customerNamed(String name, String document) {
        return new CustomerDetails(
                name,
                new NationalId(document),
                LocalDate.of(1990, 1, 1),
                new ContactDetails(new PhoneNumber("41999990000"), null),
                null);
    }

    private WeeklySchedule businessHours() {
        TimeRange hours = new TimeRange(LocalTime.of(8, 0), LocalTime.of(18, 0));
        return new WeeklySchedule(List.of(
                new AvailabilityPeriod(Weekday.of(DayOfWeek.MONDAY), hours),
                new AvailabilityPeriod(Weekday.of(DayOfWeek.TUESDAY), hours),
                new AvailabilityPeriod(Weekday.of(DayOfWeek.WEDNESDAY), hours)));
    }
}
