package com.example.clivoapi.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.billing.BillingService;
import com.example.clivoapi.core.billing.ReportPeriod;
import com.example.clivoapi.support.Clinic;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TenantIsolationScenarios extends ScenarioTest {

    private static final String ONLY_AT_THE_FIRST_CLINIC = "Carla Souza";

    @Autowired
    private BillingService billing;

    @Test
    void ct01_aCustomerSearchNeverReachesAnotherClinic() {
        Clinic first = openClinic("TEST-CT01-A");
        registerCustomer(ONLY_AT_THE_FIRST_CLINIC);
        Clinic second = openClinic("TEST-CT01-B");

        assertThat(customers.search(ONLY_AT_THE_FIRST_CLINIC)).isEmpty();

        enter(first);
        assertThat(customers.search(ONLY_AT_THE_FIRST_CLINIC))
                .extracting(customer -> customer.details().name())
                .containsExactly(ONLY_AT_THE_FIRST_CLINIC);
        assertThat(second.id()).isNotEqualTo(first.id());
    }

    @Test
    void ct02_theIdentifierOfAnotherClinicFindsNothing() {
        Clinic first = openClinic("TEST-CT02-A");
        UUID customerOfTheFirstClinic = first.customerId();
        openClinic("TEST-CT02-B");

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> customers.findOne(customerOfTheFirstClinic));
    }

    @Test
    void ct03_theFinancialReportCountsOnlyTheInvoicesOfItsOwnClinic() {
        Clinic first = openClinic("TEST-CT03-A");
        UUID anamnesisOfTheFirst = anamnesisTemplate();
        completeAnEncounter(first, anamnesisOfTheFirst);
        completeAnEncounter(first, anamnesisOfTheFirst);
        Clinic second = openClinic("TEST-CT03-B");
        completeAnEncounter(second, anamnesisTemplate());

        assertThat(billing.reportOf(thisMonth(), Role.MANAGER).invoices()).isEqualTo(1);

        enter(first);
        assertThat(billing.reportOf(thisMonth(), Role.MANAGER).invoices()).isEqualTo(2);
    }

    private ReportPeriod thisMonth() {
        return ReportPeriod.ofMonth(LocalDate.now());
    }
}
