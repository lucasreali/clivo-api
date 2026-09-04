package com.example.clivoapi.common.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.SampleEntity;
import com.example.clivoapi.common.tenant.Tenant;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditableEntityTest extends DatabaseTest {

    private static final Long SIGNED_IN_USER = 42L;

    @BeforeEach
    void signIn() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new TestAuditor(SIGNED_IN_USER), null, "ROLE_USER"));
    }

    @AfterEach
    void signOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void fillsTheFourAuditColumnsOnItsOwn() {
        Tenant clinic = createTenant("TEST-AUDIT");

        SampleEntity sample = persistSample(clinic, "first version");

        Map<String, Object> auditTrail = auditTrailOf(sample);
        assertThat(auditTrail.get("created_at")).isNotNull();
        assertThat(auditTrail.get("updated_at")).isNotNull();
        assertThat(auditTrail.get("created_by")).isEqualTo(SIGNED_IN_USER);
        assertThat(auditTrail.get("updated_by")).isEqualTo(SIGNED_IN_USER);
    }

    @Test
    void refreshesTheModificationTrailOnUpdate() {
        Tenant clinic = createTenant("TEST-AUDIT-UPDATE");
        SampleEntity sample = persistSample(clinic, "first version");
        Instant firstUpdate = updatedAtOf(sample);

        asTenant(clinic, entityManager -> {
            SampleEntity managed = entityManager.find(SampleEntity.class, sample.id());
            managed.relabel("second version");
            return managed;
        });

        assertThat(updatedAtOf(sample)).isAfter(firstUpdate);
    }

    private Instant updatedAtOf(SampleEntity sample) {
        return jdbcTemplate.queryForObject(
                "SELECT updated_at FROM sample_entity WHERE id = ?", Instant.class, sample.id());
    }

    private Map<String, Object> auditTrailOf(SampleEntity sample) {
        return jdbcTemplate.queryForMap(
                "SELECT created_at, created_by, updated_at, updated_by FROM sample_entity WHERE id = ?", sample.id());
    }

    private record TestAuditor(Long userId) implements AuditorIdentity {
    }
}
