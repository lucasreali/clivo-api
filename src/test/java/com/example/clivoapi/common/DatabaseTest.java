package com.example.clivoapi.common;

import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Function;
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
        jdbcTemplate.update("DELETE FROM sample_entity");
        jdbcTemplate.update("DELETE FROM tenant WHERE code LIKE 'TEST-%'");
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
