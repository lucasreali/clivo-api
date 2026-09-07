package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
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
import com.example.clivoapi.core.customer.PhoneNumber;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.encounter.EncounterOpening;
import com.example.clivoapi.core.encounter.EncounterService;
import com.example.clivoapi.core.practitioner.PractitionerDetails;
import com.example.clivoapi.core.practitioner.PractitionerService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class InventoryFixture extends DatabaseTest {

    protected static final ModuleCode INVENTORY = new ModuleCode("inventory");

    @Autowired
    protected InventoryService inventory;

    @Autowired
    protected EncounterService encounters;

    @Autowired
    private ModuleActivationService modules;

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

    protected Tenant openClinicWithInventory(String code) {
        Tenant clinic = openClinic(code);
        activate(INVENTORY);
        return clinic;
    }

    protected void activate(ModuleCode module) {
        modules.activate(module);
    }

    protected UUID registerProduct(ProductDetails details) {
        return inventory.register(details).id();
    }

    protected Tenant openClinic(String code) {
        Tenant clinic = createTenant(code);
        bindTenant(clinic);
        signInAsManagerOf(clinic);
        return clinic;
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    protected UUID registerGauze(String minimum) {
        return inventory
                .register(new ProductDetails("Gaze estéril", new MeasurementUnit("un"), Quantity.of(minimum), false))
                .id();
    }

    protected UUID anOpenEncounter() {
        UUID customerId = customers.register(new CustomerDetails(
                        "Ana Prado", null, LocalDate.of(1990, 1, 1), new ContactDetails(new PhoneNumber("41999990000"), null), null))
                .id();
        UUID practitionerId = practitioners
                .register(new PractitionerDetails("Dra. Marina", null))
                .id();
        UUID serviceId = catalogue
                .register(new ServiceDetails("Curativo", ServiceDuration.ofMinutes(30), Money.of("120.00")))
                .id();
        return encounters
                .open(EncounterOpening.walkIn(customerId, practitionerId, serviceId, publishedTemplate()))
                .id();
    }

    private UUID publishedTemplate() {
        TemplateContent content = new TemplateContent(List.of(new SectionContent(
                "Complaint",
                List.of(new FieldContent("complaint", "Complaint", "LONG_TEXT", null, false, List.of(), Map.of(), null)))));
        return templates.publish(templates.draft("Anamnesis", null, content).id()).id();
    }

    private void signInAsManagerOf(Tenant clinic) {
        UserSummary manager = access.registerIn(
                clinic,
                new UserRegistration(
                        "Marina Gestora",
                        new EmailAddress("gestora@clivo.test"),
                        new RawPassword("segredo123"),
                        Role.MANAGER));
        AuthenticatedUser identity = new AuthenticatedUser(manager.id(), clinic.id(), manager.name(), Role.MANAGER);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        identity, null, List.of(new SimpleGrantedAuthority(Role.MANAGER.authority()))));
    }
}
