package com.example.clivoapi.support;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.ParameterValue;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.configuration.parameter.ClinicParameterService;
import com.example.clivoapi.configuration.template.FieldContent;
import com.example.clivoapi.configuration.template.RecordTemplateService;
import com.example.clivoapi.configuration.template.SectionContent;
import com.example.clivoapi.configuration.template.TemplateContent;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;
import com.example.clivoapi.core.access.UserSummary;
import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.catalog.ServiceDetails;
import com.example.clivoapi.core.catalog.ServiceDuration;
import com.example.clivoapi.core.customer.ContactDetails;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.customer.NationalId;
import com.example.clivoapi.core.encounter.EncounterOpening;
import com.example.clivoapi.core.encounter.EncounterService;
import com.example.clivoapi.core.practitioner.AvailabilityPeriod;
import com.example.clivoapi.core.practitioner.PractitionerDetails;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.core.practitioner.TimeRange;
import com.example.clivoapi.core.practitioner.Weekday;
import com.example.clivoapi.core.practitioner.WeeklySchedule;
import com.example.clivoapi.core.scheduling.AppointmentBooking;
import com.example.clivoapi.core.scheduling.AppointmentSnapshot;
import com.example.clivoapi.core.scheduling.SchedulingService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ClinicFixture extends DatabaseTest {

    protected static final int SERVICE_MINUTES = 60;

    protected static final LocalTime OPENS_AT = LocalTime.of(8, 0);

    protected static final LocalTime CLOSES_AT = LocalTime.of(18, 0);

    @Autowired
    protected ModuleActivationService modules;

    @Autowired
    protected ClinicParameterService parameters;

    @Autowired
    protected RecordTemplateService templates;

    @Autowired
    protected CustomerService customers;

    @Autowired
    protected PractitionerService practitioners;

    @Autowired
    protected CatalogService catalogue;

    @Autowired
    protected SchedulingService scheduling;

    @Autowired
    protected EncounterService encounters;

    @Autowired
    private AccessService access;

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    protected Clinic openClinic(String code) {
        Tenant tenant = createTenant(code);
        bindTenant(tenant);
        signInAs(tenant, Role.MANAGER);
        return new Clinic(tenant, registerCustomer("Ana Prado"), registerPractitioner("Dra. Marina"), registerService());
    }

    protected void enter(Clinic clinic) {
        enterAs(clinic, Role.MANAGER);
    }

    protected void enterAs(Clinic clinic, Role role) {
        bindTenant(clinic.tenant());
        signInAs(clinic.tenant(), role);
    }

    protected void activate(ModuleCode module) {
        modules.activate(module);
    }

    protected void change(ParameterCode parameter, String value) {
        parameters.change(parameter, ParameterValue.of(value));
    }

    protected UUID registerCustomer(String name) {
        return customers.register(new CustomerDetails(
                        name,
                        new NationalId(documentOf(name)),
                        LocalDate.of(1990, 1, 1),
                        new ContactDetails("41999990000", null),
                        null))
                .id();
    }

    protected UUID registerPractitioner(String name) {
        UUID id = practitioners.register(new PractitionerDetails(name, null)).id();
        practitioners.follow(id, businessHours());
        return id;
    }

    protected UUID registerService() {
        return catalogue
                .register(new ServiceDetails(
                        "Limpeza", ServiceDuration.ofMinutes(SERVICE_MINUTES), Money.of("180.00")))
                .id();
    }

    protected UUID publishTemplate(String name, TemplateContent content) {
        return templates.publish(templates.draft(name, null, content).id()).id();
    }

    protected TemplateContent sectionWith(String sectionName, FieldContent... fields) {
        return new TemplateContent(List.of(new SectionContent(sectionName, List.of(fields))));
    }

    protected FieldContent fieldOf(String code, String fieldType) {
        return new FieldContent(code, code, fieldType, null, false, List.of(), Map.of(), null);
    }

    protected AppointmentSnapshot bookAt(Clinic clinic, LocalDateTime start) {
        return scheduling.schedule(new AppointmentBooking(
                clinic.customerId(), clinic.practitionerId(), clinic.serviceId(), start));
    }

    protected UUID openEncounter(Clinic clinic, UUID templateId) {
        return encounters
                .open(EncounterOpening.walkIn(
                        clinic.customerId(), clinic.practitionerId(), clinic.serviceId(), templateId))
                .id();
    }

    protected LocalDateTime nextWeekAt(DayOfWeek day, String time) {
        LocalDate date = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(day));
        return LocalDateTime.of(date, LocalTime.parse(time));
    }

    private void signInAs(Tenant clinic, Role role) {
        UserSummary user = knownUser(role).orElseGet(() -> registerUser(clinic, role));
        AuthenticatedUser identity = new AuthenticatedUser(user.id(), clinic.id(), user.name(), role);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        identity, null, List.of(new SimpleGrantedAuthority(role.authority()))));
    }

    private Optional<UserSummary> knownUser(Role role) {
        return access.usersOfCurrentClinic().stream()
                .filter(user -> user.role() == role)
                .findFirst();
    }

    private UserSummary registerUser(Tenant clinic, Role role) {
        return access.registerIn(
                clinic,
                new UserRegistration(
                        role.name(), new EmailAddress(emailOf(role)), new RawPassword("segredo123"), role));
    }

    private String emailOf(Role role) {
        return "%s@clivo.test".formatted(role.name().toLowerCase());
    }

    private String documentOf(String name) {
        return String.valueOf(10000000000L + Math.abs(name.hashCode() % 10000000));
    }

    private WeeklySchedule businessHours() {
        TimeRange hours = new TimeRange(OPENS_AT, CLOSES_AT);
        return new WeeklySchedule(Arrays.stream(DayOfWeek.values())
                .map(day -> new AvailabilityPeriod(Weekday.of(day), hours))
                .toList());
    }
}
