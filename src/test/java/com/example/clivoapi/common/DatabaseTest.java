package com.example.clivoapi.common;

import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;
import com.example.clivoapi.core.access.UserSummary;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class DatabaseTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private AccessService accessService;

    @AfterEach
    void discardTestData() {
        SecurityContextHolder.clearContext();
        tenantContext.clear();
        jdbcTemplate.update("DELETE FROM sample_entity");
        jdbcTemplate.update("DELETE FROM payment");
        jdbcTemplate.update("DELETE FROM invoice_item");
        jdbcTemplate.update("DELETE FROM invoice");
        jdbcTemplate.update("DELETE FROM commission");
        jdbcTemplate.update("DELETE FROM commission_rate");
        jdbcTemplate.update("DELETE FROM customer_insurance");
        jdbcTemplate.update("DELETE FROM insurance_plan");
        jdbcTemplate.update("DELETE FROM package_usage");
        jdbcTemplate.update("DELETE FROM session_package");
        jdbcTemplate.update("DELETE FROM stock_movement");
        jdbcTemplate.update("DELETE FROM batch");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM attachment");
        jdbcTemplate.update("DELETE FROM encounter");
        jdbcTemplate.update("DELETE FROM template_field");
        jdbcTemplate.update("DELETE FROM template_section");
        jdbcTemplate.update("DELETE FROM record_template");
        jdbcTemplate.update("DELETE FROM tenant_module_history");
        jdbcTemplate.update("DELETE FROM tenant_module");
        jdbcTemplate.update("DELETE FROM tenant_parameter");
        jdbcTemplate.update("DELETE FROM notification");
        jdbcTemplate.update("DELETE FROM appointment");
        jdbcTemplate.update("DELETE FROM schedule_block");
        jdbcTemplate.update("DELETE FROM service");
        jdbcTemplate.update("DELETE FROM availability");
        jdbcTemplate.update("DELETE FROM practitioner");
        jdbcTemplate.update("DELETE FROM dependent");
        jdbcTemplate.update("DELETE FROM clinical_alert");
        jdbcTemplate.update("DELETE FROM consent");
        jdbcTemplate.update("DELETE FROM customer");
        jdbcTemplate.update("DELETE FROM module_grant");
        jdbcTemplate.update("DELETE FROM app_user");
        jdbcTemplate.update("DELETE FROM tenant WHERE name LIKE 'TEST-%'");
    }

    protected UserSummary signInAs(Tenant clinic, Role role) {
        UserSummary user = accessService.registerIn(clinic, registrationOf(clinic, role));
        authenticate(user.id(), clinic.id(), user.name(), role);
        return user;
    }

    protected void authenticate(UUID userId, UUID clinicId, String name, Role role) {
        AuthenticatedUser identity = new AuthenticatedUser(userId, clinicId, name, role);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        identity, null, List.of(new SimpleGrantedAuthority(role.authority()))));
    }

    private UserRegistration registrationOf(Tenant clinic, Role role) {
        return new UserRegistration(
                role.name(),
                new EmailAddress("%s-%s@clivo.test".formatted(role.name().toLowerCase(Locale.ROOT), UUID.randomUUID())),
                new RawPassword("segredo123"),
                role);
    }

    protected void bindTenant(Tenant tenant) {
        tenantContext.bind(tenant.id());
    }

    protected void inTenant(Tenant tenant, Runnable work) {
        valueInTenant(tenant, () -> {
            work.run();
            return null;
        });
    }

    protected <T> T valueInTenant(Tenant tenant, Supplier<T> work) {
        tenantContext.bind(tenant.id());
        try {
            return work.get();
        } finally {
            tenantContext.clear();
        }
    }

    protected Tenant createTenant(String name) {
        return inNewSession(entityManager -> {
            Tenant tenant = new Tenant(name);
            entityManager.persist(tenant);
            return tenant;
        });
    }

    protected SampleEntity persistSample(Tenant tenant, String label) {
        return asTenant(tenant, entityManager -> {
            SampleEntity sample = new SampleEntity(label);
            entityManager.persist(sample);
            return sample;
        });
    }

    protected List<String> labelsVisibleTo(Tenant tenant) {
        return asTenant(tenant, entityManager -> entityManager
                .createQuery("select sample.label from SampleEntity sample", String.class)
                .getResultList());
    }

    protected <T> T asTenant(Tenant tenant, Function<EntityManager, T> work) {
        tenantContext.bind(tenant.id());
        try {
            return inNewSession(work);
        } finally {
            tenantContext.clear();
        }
    }

    protected <T> T inNewSession(Function<EntityManager, T> work) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            T result = work.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } finally {
            entityManager.close();
        }
    }
}
