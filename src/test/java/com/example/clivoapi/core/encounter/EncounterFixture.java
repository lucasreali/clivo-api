package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.RecordTemplateService;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.core.access.Role;
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
import com.example.clivoapi.core.scheduling.AppointmentBooking;
import com.example.clivoapi.core.scheduling.SchedulingService;
import com.example.clivoapi.support.GeneratedDocument;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

abstract class EncounterFixture extends DatabaseTest {

    @Autowired
    protected EncounterService encounters;

    @Autowired
    protected RecordTemplateService templates;

    @Autowired
    protected SchedulingService scheduling;

    @Autowired
    private CustomerService customers;

    @Autowired
    private PractitionerService practitioners;

    @Autowired
    private CatalogService catalogue;

    private Tenant clinic;
    private UUID customerId;
    private UUID practitionerId;
    private UUID serviceId;

    protected Tenant openClinic(String code) {
        clinic = createTenant(code);
        bindTenant(clinic);
        customerId = customers.register(customerNamed("Ana Prado", GeneratedDocument.nationalIdTextFor("Ana Prado"))).id();
        practitionerId = practitioners.register(new PractitionerDetails("Dr. Marina", null)).id();
        practitioners.follow(practitionerId, businessHours());
        serviceId = catalogue
                .register(new ServiceDetails("Limpeza", ServiceDuration.ofMinutes(60), Money.of("180.00")))
                .id();
        return clinic;
    }

    protected Tenant clinic() {
        return clinic;
    }

    protected UUID signIn(Role role) {
        return signInAs(clinic, role).id();
    }

    protected EncounterSnapshot reopen(UUID encounterId) {
        return encounters.findOne(encounterId, Role.PRACTITIONER);
    }

    protected EncounterSnapshot completeAsPractitioner(UUID encounterId) {
        return encounters.complete(encounterId, Role.PRACTITIONER);
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

    protected UUID publishTemplate(String name, TemplateContent content) {
        UUID draftId = templates.draft(name, null, content).id();
        return templates.publish(draftId).id();
    }

    protected UUID bookAppointment() {
        LocalDate monday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime start = LocalDateTime.of(monday, LocalTime.of(9, 0));
        return scheduling.schedule(new AppointmentBooking(customerId, practitionerId, serviceId, start)).id();
    }

    protected TemplateContent complaintWith(String fieldCode, String fieldType) {
        return new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent(fieldCode, fieldCode, fieldType, null, false, List.of(), Map.of(), null)))));
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
                new AvailabilityPeriod(Weekday.of(DayOfWeek.TUESDAY), hours)));
    }
}
