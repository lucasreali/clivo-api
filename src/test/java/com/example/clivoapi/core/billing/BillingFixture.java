package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
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
import com.example.clivoapi.core.practitioner.PractitionerDetails;
import com.example.clivoapi.core.practitioner.PractitionerService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BillingFixture extends DatabaseTest {

    protected static final String SERVICE_PRICE = "180.00";

    @Autowired
    protected BillingService billing;

    @Autowired
    protected EncounterService encounters;

    @Autowired
    private RecordTemplateService templates;

    @Autowired
    private CustomerService customers;

    @Autowired
    private PractitionerService practitioners;

    @Autowired
    private CatalogService catalogue;

    @Autowired
    private AccessService access;

    private Tenant clinic;
    private Long customerId;
    private Long practitionerId;
    private Long serviceId;
    private Long templateId;

    protected Tenant openClinic(String code) {
        clinic = createTenant(code);
        bindTenant(clinic);
        customerId = customers.register(customerNamed("Ana Prado")).id();
        practitionerId = practitioners.register(new PractitionerDetails("Dr. Marina", null)).id();
        serviceId = catalogue
                .register(new ServiceDetails("Limpeza", ServiceDuration.ofMinutes(60), Money.of(SERVICE_PRICE)))
                .id();
        templateId = publishedTemplate();
        signInAsManager();
        return clinic;
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    protected Tenant clinic() {
        return clinic;
    }

    protected Long customerId() {
        return customerId;
    }

    protected Long serviceId() {
        return serviceId;
    }

    protected Long practitionerId() {
        return practitionerId;
    }

    protected Long completeAnEncounter() {
        Long id = encounters
                .open(EncounterOpening.walkIn(customerId, practitionerId, serviceId, templateId))
                .id();
        encounters.fill(id, RecordValues.of(Map.of("complaint", "Dor no dente 26")));
        return encounters.complete(id, Role.PRACTITIONER).id();
    }

    protected InvoiceSnapshot invoiceOfACompletedEncounter() {
        return billing.findByEncounter(completeAnEncounter());
    }

    private void signInAsManager() {
        signInAs(Role.MANAGER, "gestora@clivo.test");
    }

    protected void signInAs(Role role, String email) {
        UserSummary user = access.registerIn(
                clinic,
                new UserRegistration(
                        role.name(), new EmailAddress(email), new RawPassword("segredo123"), role));
        AuthenticatedUser identity = new AuthenticatedUser(user.id(), clinic.id(), user.name(), role);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        identity, null, List.of(new SimpleGrantedAuthority(role.authority()))));
    }

    private Long publishedTemplate() {
        TemplateContent content = new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent(
                        "complaint", "Complaint", "LONG_TEXT", null, false, List.of(), Map.of(), null)))));
        Long draftId = templates.draft("Anamnesis", null, content).id();
        return templates.publish(draftId).id();
    }

    private CustomerDetails customerNamed(String name) {
        return new CustomerDetails(
                name,
                new NationalId("12345678901"),
                LocalDate.of(1990, 1, 1),
                new ContactDetails("41999990000", null),
                null);
    }
}
