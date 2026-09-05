package com.example.clivoapi.common;

import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @AfterEach
    void discardTestData() {
        tenantContext.clear();
        jdbcTemplate.update("DELETE FROM sample_entity");
        jdbcTemplate.update("DELETE FROM payment");
        jdbcTemplate.update("DELETE FROM invoice_item");
        jdbcTemplate.update("DELETE FROM invoice");
        jdbcTemplate.update("DELETE FROM package_usage");
        jdbcTemplate.update("DELETE FROM session_package");
        jdbcTemplate.update("DELETE FROM stock_movement");
        jdbcTemplate.update("DELETE FROM batch");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM encounter");
        jdbcTemplate.update("DELETE FROM template_field");
        jdbcTemplate.update("DELETE FROM template_section");
        jdbcTemplate.update("DELETE FROM record_template");
        jdbcTemplate.update("DELETE FROM tenant_module");
        jdbcTemplate.update("DELETE FROM tenant_parameter");
        jdbcTemplate.update("DELETE FROM appointment");
        jdbcTemplate.update("DELETE FROM schedule_block");
        jdbcTemplate.update("DELETE FROM service");
        jdbcTemplate.update("DELETE FROM availability");
        jdbcTemplate.update("DELETE FROM practitioner");
        jdbcTemplate.update("DELETE FROM dependent");
        jdbcTemplate.update("DELETE FROM consent");
        jdbcTemplate.update("DELETE FROM customer");
        jdbcTemplate.update("DELETE FROM app_user");
        jdbcTemplate.update("DELETE FROM tenant WHERE code LIKE 'TEST-%'");
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

    protected Tenant createTenant(String code) {
        return inNewSession(entityManager -> {
            Tenant tenant = new Tenant(code, code);
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
