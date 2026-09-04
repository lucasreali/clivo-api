package com.example.clivoapi.common.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.SampleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

class TenantIsolationTest extends DatabaseTest {

    @Test
    void queryOfOneTenantDoesNotReachTheOther() {
        Tenant firstClinic = createTenant("TEST-FIRST");
        Tenant secondClinic = createTenant("TEST-SECOND");

        persistSample(firstClinic, "record of the first clinic");
        persistSample(secondClinic, "record of the second clinic");

        assertThat(labelsVisibleTo(firstClinic)).containsExactly("record of the first clinic");
        assertThat(labelsVisibleTo(secondClinic)).containsExactly("record of the second clinic");
    }

    @Test
    void requestWithoutTenantReachesNothing() {
        Tenant clinic = createTenant("TEST-UNBOUND");
        persistSample(clinic, "record of the clinic");

        List<String> labels = inNewSession(entityManager -> entityManager
                .createQuery("select sample.label from SampleEntity sample", String.class)
                .getResultList());

        assertThat(labels).isEmpty();
    }

    @Test
    void entityKeepsTheTenantItWasCreatedIn() {
        Tenant clinic = createTenant("TEST-OWNER");

        SampleEntity sample = persistSample(clinic, "record of the clinic");

        assertThat(sample.belongsTo(clinic)).isTrue();
    }
}
