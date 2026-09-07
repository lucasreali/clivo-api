package com.example.clivoapi.modules.sessionpackage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.core.billing.BillingFixture;
import com.example.clivoapi.core.billing.InvoiceCoverage;
import com.example.clivoapi.core.billing.InvoiceSnapshot;
import com.example.clivoapi.core.billing.InvoiceStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SessionPackageServiceTest extends BillingFixture {

    private static final ModuleCode SESSION_PACKAGE = new ModuleCode("sessionpackage");

    @Autowired
    private SessionPackageService packages;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void aPackageIsSoldWithEverySessionStillToUse() {
        openClinicWithPackages("TEST-PKG-SELL");

        SessionPackageSnapshot sold = sellTenSessions();

        assertThat(sold.remainingSessions()).isEqualTo(10);
        assertThat(sold.status()).isEqualTo(SessionPackageStatus.ACTIVE);
        assertThat(sold.active()).isTrue();
    }

    @Test
    void aCompletedEncounterConsumesOneSessionAndLeavesNothingToPay() {
        openClinicWithPackages("TEST-PKG-CONSUME");
        SessionPackageSnapshot sold = sellTenSessions();

        UUID encounterId = completeAnEncounter();

        assertThat(packages.findOne(sold.id()).remainingSessions()).isEqualTo(9);
        InvoiceSnapshot invoice = billing.findByEncounter(encounterId);
        assertThat(invoice.coverage()).isEqualTo(InvoiceCoverage.SESSION_PACKAGE);
        assertThat(invoice.amounts().net()).isEqualTo(Money.zero());
        assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void anEncounterNeverConsumesMoreThanOneSession() {
        openClinicWithPackages("TEST-PKG-ONCE");
        SessionPackageSnapshot sold = sellTenSessions();
        UUID encounterId = completeAnEncounter();

        assertThatThrownBy(() -> packages.consumeFor(completionOf(encounterId)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("encounter %s already consumed a session of this package".formatted(encounterId));
        assertThat(packages.findOne(sold.id()).usedSessions()).isEqualTo(1);
    }

    @Test
    void aPackageWithoutSessionsLeftStopsCoveringEncounters() {
        openClinicWithPackages("TEST-PKG-EMPTY");
        SessionPackageSnapshot sold = sellOneSession();

        completeAnEncounter();
        UUID secondEncounter = completeAnEncounter();

        assertThat(packages.findOne(sold.id()).status()).isEqualTo(SessionPackageStatus.EXHAUSTED);
        assertThat(billing.findByEncounter(secondEncounter).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
    }

    @Test
    void aCancelledPackageNoLongerCoversAnything() {
        openClinicWithPackages("TEST-PKG-CANCEL");
        SessionPackageSnapshot sold = sellTenSessions();
        packages.cancel(sold.id());

        UUID encounterId = completeAnEncounter();

        assertThat(billing.findByEncounter(encounterId).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
    }

    @Test
    void aClinicWithoutTheModuleKeepsBillingEveryEncounterInFull() {
        openClinic("TEST-PKG-OFF");
        sellTenSessions();

        UUID encounterId = completeAnEncounter();

        assertThat(billing.findByEncounter(encounterId).coverage()).isEqualTo(InvoiceCoverage.DIRECT);
    }

    @Test
    void aPackageAlreadyExpiredIsNotSold() {
        assertThatThrownBy(() -> new PackagePurchase(
                        UUID.randomUUID(), UUID.randomUUID(), new SessionCount(5), Money.of("500.00"), LocalDate.now().minusDays(1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("expiresOn: a package cannot be sold already expired");
    }

    private CompletedEncounter completionOf(UUID encounterId) {
        return new CompletedEncounter(encounterId, customerId(), practitionerId(), serviceId());
    }

    private void openClinicWithPackages(String code) {
        openClinic(code);
        modules.activate(SESSION_PACKAGE);
    }

    private SessionPackageSnapshot sellTenSessions() {
        return packages.sell(purchaseOf(10));
    }

    private SessionPackageSnapshot sellOneSession() {
        return packages.sell(purchaseOf(1));
    }

    private PackagePurchase purchaseOf(int sessions) {
        return new PackagePurchase(
                customerId(),
                serviceId(),
                new SessionCount(sessions),
                Money.of("1500.00"),
                LocalDate.now().plusMonths(6));
    }
}
