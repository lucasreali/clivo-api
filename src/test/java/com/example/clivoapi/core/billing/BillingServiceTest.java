package com.example.clivoapi.core.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.Tenant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BillingServiceTest extends BillingFixture {

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-BILLING");
    }

    @Test
    void completingAnEncounterIssuesAnOpenInvoiceWorthTheService() {
        InvoiceSnapshot invoice = invoiceOfACompletedEncounter();

        assertThat(invoice.status()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.coverage()).isEqualTo(InvoiceCoverage.DIRECT);
        assertThat(invoice.amounts().gross()).isEqualTo(Money.of(SERVICE_PRICE));
        assertThat(invoice.amounts().net()).isEqualTo(Money.of(SERVICE_PRICE));
        assertThat(invoice.amounts().outstanding()).isEqualTo(Money.of(SERVICE_PRICE));
    }

    @Test
    void theInvoiceCarriesTheServiceItCharges() {
        InvoiceSnapshot invoice = invoiceOfACompletedEncounter();

        assertThat(invoice.lines()).singleElement().satisfies(line -> {
            assertThat(line.description()).isEqualTo("Limpeza");
            assertThat(line.serviceId()).isEqualTo(serviceId());
            assertThat(line.total()).isEqualTo(Money.of(SERVICE_PRICE));
        });
    }

    @Test
    void anOpenEncounterIsNotInvoicedYet() {
        UUID customerId = customerId();

        assertThat(billing.findByCustomer(customerId)).isEmpty();
    }

    @Test
    void aDiscountLowersTheNetAmountAndStatesItsReason() {
        InvoiceSnapshot issued = invoiceOfACompletedEncounter();

        InvoiceSnapshot discounted =
                billing.applyDiscount(issued.id(), Money.of("30.00"), new DiscountReason("Cliente antigo"));

        assertThat(discounted.amounts().net()).isEqualTo(Money.of("150.00"));
        assertThat(discounted.amounts().outstanding()).isEqualTo(Money.of("150.00"));
        assertThat(discounted.reasonGiven()).contains("Cliente antigo");
    }

    @Test
    void aDiscountWithoutAReasonIsRefused() {
        InvoiceSnapshot issued = invoiceOfACompletedEncounter();

        assertThatThrownBy(() -> billing.applyDiscount(issued.id(), Money.of("30.00"), new DiscountReason(" ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a reason is required to discount an invoice");
    }

    @Test
    void aDiscountAboveTheInvoicedAmountIsRefused() {
        InvoiceSnapshot issued = invoiceOfACompletedEncounter();

        assertThatThrownBy(() ->
                        billing.applyDiscount(issued.id(), Money.of("300.00"), new DiscountReason("Erro de digitacao")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a discount of 300.00 is above the invoiced amount of 180.00");
    }

    @Test
    void anInvoiceIssuedTodayIsNotOverdue() {
        assertThat(invoiceOfACompletedEncounter().overdue()).isFalse();
    }

    @Test
    void aSecondEncounterOfTheSameCustomerIsInvoicedOnItsOwn() {
        completeAnEncounter();
        completeAnEncounter();

        assertThat(billing.findByCustomer(customerId())).hasSize(2);
    }

    @Test
    void anInvoiceOfOneClinicDoesNotReachTheOther() {
        InvoiceSnapshot issued = invoiceOfACompletedEncounter();
        Tenant other = createTenant("TEST-BILLING-OTHER");

        assertThatThrownBy(() -> valueInTenant(other, () -> billing.findOne(issued.id())))
                .hasMessageContaining("Invoice");
    }
}
