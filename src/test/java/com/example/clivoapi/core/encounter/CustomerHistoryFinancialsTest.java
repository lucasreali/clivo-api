package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.clivoapi.common.extension.ChargeState;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.billing.BillingFixture;
import com.example.clivoapi.core.billing.PaymentDetails;
import com.example.clivoapi.core.billing.PaymentMethod;
import com.example.clivoapi.modules.insurance.CoveragePercentage;
import com.example.clivoapi.modules.insurance.InsuranceService;
import com.example.clivoapi.modules.insurance.MemberNumber;
import com.example.clivoapi.modules.insurance.PlanDetails;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerHistoryFinancialsTest extends BillingFixture {

    private static final ModuleCode INSURANCE = new ModuleCode("insurance");

    @Autowired
    private InsuranceService insurance;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void anEncounterFullyReimbursedByThePlanReportsAsCoveredByInsurance() {
        openClinic("TEST-HIST-INS");
        modules.activate(INSURANCE);
        enrolIn(registerPlan("Unimed", "100"));

        completeAnEncounter();

        HistoryEntry entry = onlyEntry();
        assertThat(entry.charge().state()).isEqualTo(ChargeState.COVERED);
        assertThat(entry.charge().coveredBy()).contains("INSURANCE");
        assertThat(entry.coveredBy()).hasValueSatisfying(note -> assertThat(note.plan()).isEqualTo("Unimed"));
    }

    @Test
    void theSameEncounterInAClinicWithoutTheModuleCarriesNoInsuranceAtAll() {
        openClinic("TEST-HIST-NOINS");

        completeAnEncounter();

        HistoryEntry entry = onlyEntry();
        assertThat(entry.charge().state()).isEqualTo(ChargeState.OUTSTANDING);
        assertThat(entry.charge().outstanding()).isEqualTo(Money.of(SERVICE_PRICE));
        assertThat(entry.charge().coveredBy()).isEmpty();
        assertThat(entry.coveredBy()).isEmpty();
    }

    @Test
    void aSettledEncounterReportsAsPaidAndFeedsTheFinancialSummary() {
        openClinic("TEST-HIST-PAID");
        UUID encounterId = completeAnEncounter();
        billing.settle(
                billing.findByEncounter(encounterId).id(),
                new PaymentDetails(Money.of(SERVICE_PRICE), PaymentMethod.CASH));

        CustomerHistory history = history();

        assertThat(onlyEntry().charge().state()).isEqualTo(ChargeState.PAID);
        assertThat(history.financials().paid()).isEqualTo(Money.of(SERVICE_PRICE));
        assertThat(history.financials().outstanding()).isEqualTo(Money.zero());
    }

    @Test
    void anEncounterThatWasNeverBilledCarriesNoCharge() {
        openClinic("TEST-HIST-NOCHARGE");
        openAnEncounter();

        assertThat(onlyEntry().charge().state()).isEqualTo(ChargeState.NO_CHARGE);
        assertThat(history().financials().outstanding()).isEqualTo(Money.zero());
    }

    private HistoryEntry onlyEntry() {
        return history().entries().getFirst();
    }

    private CustomerHistory history() {
        return encounters.historyOf(customerId(), Role.PRACTITIONER);
    }

    private UUID registerPlan(String name, String reimbursement) {
        return insurance
                .register(new PlanDetails(name, CoveragePercentage.of(reimbursement)))
                .id();
    }

    private void enrolIn(UUID planId) {
        insurance.enrol(customerId(), planId, new MemberNumber("123456"));
    }
}
