package com.example.clivoapi.modules.commission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.core.billing.BillingFixture;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommissionServiceTest extends BillingFixture {

    private static final ModuleCode COMMISSION = new ModuleCode("commission");

    @Autowired
    private CommissionService commissions;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void aPractitionerEarnsTheShareTheClinicAgreedOn() {
        openClinicWithCommission("TEST-COMM-RATE");

        PractitionerCommissionSnapshot earner = chargeAt("30");

        assertThat(earner.rate().appliedTo(Money.of("180.00"))).isEqualTo(Money.of("54.00"));
    }

    @Test
    void aCompletedEncounterEarnsItsCommissionOnWhatWasBilled() {
        openClinicWithCommission("TEST-COMM-EARN");
        chargeAt("30");

        Long encounterId = completeAnEncounter();

        CommissionStatement statement = thisMonth();
        assertThat(statement.commissions()).hasSize(1);
        assertThat(statement.commissions().getFirst().encounterId()).isEqualTo(encounterId);
        assertThat(statement.total()).isEqualTo(Money.of("54.00"));
        assertThat(statement.isClosed()).isFalse();
    }

    @Test
    void aPractitionerWithoutAnAgreedShareEarnsNothing() {
        openClinicWithCommission("TEST-COMM-NONE");

        completeAnEncounter();

        assertThat(thisMonth().commissions()).isEmpty();
        assertThat(thisMonth().total()).isEqualTo(Money.zero());
    }

    @Test
    void aRateOfZeroLeavesNoCommissionBehind() {
        openClinicWithCommission("TEST-COMM-ZERO");
        chargeAt("0");

        completeAnEncounter();

        assertThat(thisMonth().commissions()).isEmpty();
    }

    @Test
    void closingThePeriodSettlesEveryCommissionInIt() {
        openClinicWithCommission("TEST-COMM-CLOSE");
        chargeAt("30");
        completeAnEncounter();
        completeAnEncounter();

        CommissionStatement closed = commissions.close(currentPeriod());

        assertThat(closed.total()).isEqualTo(Money.of("108.00"));
        assertThat(closed.isClosed()).isTrue();
        assertThat(closed.commissions())
                .allSatisfy(commission -> assertThat(commission.status()).isEqualTo(CommissionStatus.CLOSED));
    }

    @Test
    void aPeriodAlreadySettledIsNotClosedAgain() {
        openClinicWithCommission("TEST-COMM-TWICE");
        chargeAt("30");
        completeAnEncounter();
        commissions.close(currentPeriod());

        assertThatThrownBy(() -> commissions.close(currentPeriod()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("period %s has no open commission to close".formatted(currentPeriod()));
    }

    @Test
    void aClinicWithoutTheModuleEarnsNoCommission() {
        openClinic("TEST-COMM-OFF");
        chargeAt("30");

        completeAnEncounter();

        assertThat(thisMonth().commissions()).isEmpty();
    }

    @Test
    void aRateOutsideTheAllowedRangeIsRefused() {
        assertThatThrownBy(() -> CommissionRate.of("140"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a commission rate lies between 0 and 100");
    }

    private void openClinicWithCommission(String code) {
        openClinic(code);
        modules.activate(COMMISSION);
    }

    private PractitionerCommissionSnapshot chargeAt(String percentage) {
        return commissions.chargeAt(practitionerId(), CommissionRate.of(percentage));
    }

    private CommissionStatement thisMonth() {
        return commissions.statementOf(currentPeriod());
    }

    private CommissionPeriod currentPeriod() {
        return CommissionPeriod.covering(LocalDate.now());
    }
}
