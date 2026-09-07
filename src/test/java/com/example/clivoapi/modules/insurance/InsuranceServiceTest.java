package com.example.clivoapi.modules.insurance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.core.billing.BillingFixture;
import com.example.clivoapi.core.billing.InvoiceCoverage;
import com.example.clivoapi.core.billing.InvoiceSnapshot;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class InsuranceServiceTest extends BillingFixture {

    private static final ModuleCode INSURANCE = new ModuleCode("insurance");

    @Autowired
    private InsuranceService insurance;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void aPlanReimbursesTheShareItDeclares() {
        openClinicWithInsurance("TEST-INS-PCT");

        InsurancePlanSnapshot plan = registerPlan("Unimed", "70");

        assertThat(plan.details().reimbursement().appliedTo(Money.of("180.00"))).isEqualTo(Money.of("126.00"));
    }

    @Test
    void anInsuredCustomerIsBilledOnlyForTheShareThePlanDoesNotCover() {
        openClinicWithInsurance("TEST-INS-BILL");
        enrol(registerPlan("Unimed", "70"));

        UUID encounterId = completeAnEncounter();

        InvoiceSnapshot invoice = billing.findByEncounter(encounterId);
        assertThat(invoice.coverage()).isEqualTo(InvoiceCoverage.INSURANCE);
        assertThat(invoice.amounts().discount()).isEqualTo(Money.of("126.00"));
        assertThat(invoice.amounts().net()).isEqualTo(Money.of("54.00"));
        assertThat(invoice.reasonGiven()).contains("reimbursed by Unimed");
    }

    @Test
    void aCustomerWithoutAMembershipIsBilledInFull() {
        openClinicWithInsurance("TEST-INS-NONE");
        registerPlan("Unimed", "70");

        UUID encounterId = completeAnEncounter();

        assertThat(billing.findByEncounter(encounterId).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
        assertThat(billing.findByEncounter(encounterId).amounts().net()).isEqualTo(Money.of("180.00"));
    }

    @Test
    void aDeactivatedPlanStopsReimbursing() {
        openClinicWithInsurance("TEST-INS-OFFPLAN");
        InsurancePlanSnapshot plan = registerPlan("Unimed", "70");
        enrol(plan);
        insurance.deactivate(plan.id());

        UUID encounterId = completeAnEncounter();

        assertThat(billing.findByEncounter(encounterId).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
    }

    @Test
    void aClinicWithoutTheModuleBillsEveryEncounterInFull() {
        openClinic("TEST-INS-OFF");
        enrol(registerPlan("Unimed", "70"));

        UUID encounterId = completeAnEncounter();

        assertThat(billing.findByEncounter(encounterId).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
        assertThat(billing.findByEncounter(encounterId).amounts().net()).isEqualTo(Money.of("180.00"));
    }

    @Test
    void theSameCustomerIsNotEnrolledTwiceInOnePlan() {
        openClinicWithInsurance("TEST-INS-TWICE");
        InsurancePlanSnapshot plan = registerPlan("Unimed", "70");
        enrol(plan);

        assertThatThrownBy(() -> enrol(plan))
                .isInstanceOf(BusinessException.class)
                .hasMessage("this customer already carries a membership of that plan");
    }

    @Test
    void aPercentageOutsideTheAllowedRangeIsRefused() {
        assertThatThrownBy(() -> CoveragePercentage.of("120"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a reimbursement percentage lies between 0 and 100");
    }

    private void openClinicWithInsurance(String code) {
        openClinic(code);
        modules.activate(INSURANCE);
    }

    private InsurancePlanSnapshot registerPlan(String name, String percentage) {
        return insurance.register(new PlanDetails(name, CoveragePercentage.of(percentage)));
    }

    private CustomerInsuranceSnapshot enrol(InsurancePlanSnapshot plan) {
        return insurance.enrol(customerId(), plan.id(), new MemberNumber("0001-2233"));
    }
}
