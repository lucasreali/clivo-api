package com.example.clivoapi.core.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTest extends BillingFixture {

    private UUID invoiceId;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-PAYMENT");
        invoiceId = invoiceOfACompletedEncounter().id();
    }

    @Test
    void payingInFullSettlesTheInvoice() {
        InvoiceSnapshot settled = pay(SERVICE_PRICE);

        assertThat(settled.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(settled.amounts().outstanding()).isEqualTo(Money.zero());
    }

    @Test
    void payingPartOfItLeavesTheRest() {
        InvoiceSnapshot settled = pay("80.00");

        assertThat(settled.status()).isEqualTo(InvoiceStatus.PARTIAL);
        assertThat(settled.amounts().outstanding()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void twoPartialPaymentsAddUpToTheWhole() {
        pay("80.00");

        InvoiceSnapshot settled = pay("100.00");

        assertThat(settled.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(settled.payments()).hasSize(2);
    }

    @Test
    void aPaymentAboveTheOutstandingBalanceIsRefused() {
        assertThatThrownBy(() -> pay("200.00"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a payment of 200.00 is above the outstanding balance of 180.00");
    }

    @Test
    void aPaymentAboveWhatIsLeftAfterAPartialOneIsRefused() {
        pay("80.00");

        assertThatThrownBy(() -> pay("120.00"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a payment of 120.00 is above the outstanding balance of 100.00");
    }

    @Test
    void aRefundPutsTheBalanceBack() {
        InvoiceSnapshot settled = pay(SERVICE_PRICE);
        UUID paymentId = onlyPaymentOf(settled).id();

        InvoiceSnapshot refunded =
                billing.refund(invoiceId, paymentId, new RefundReason("Procedimento nao realizado"));

        assertThat(refunded.status()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(refunded.amounts().outstanding()).isEqualTo(Money.of(SERVICE_PRICE));
        assertThat(onlyPaymentOf(refunded).isRefunded()).isTrue();
        assertThat(onlyPaymentOf(refunded).reasonGiven()).contains("Procedimento nao realizado");
    }

    @Test
    void aRefundWithoutAReasonIsRefused() {
        UUID paymentId = onlyPaymentOf(pay(SERVICE_PRICE)).id();

        assertThatThrownBy(() -> billing.refund(invoiceId, paymentId, new RefundReason(" ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a reason is required to refund a payment");
    }

    @Test
    void refundingTheSamePaymentTwiceIsRefused() {
        UUID paymentId = onlyPaymentOf(pay(SERVICE_PRICE)).id();
        billing.refund(invoiceId, paymentId, new RefundReason("Cobranca em duplicidade"));

        assertThatThrownBy(() -> billing.refund(invoiceId, paymentId, new RefundReason("De novo")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("payment %s was already refunded".formatted(paymentId));
    }

    @Test
    void aPaymentOfAnotherInvoiceIsNotAcceptedHere() {
        UUID paymentId = onlyPaymentOf(pay(SERVICE_PRICE)).id();
        UUID otherInvoice = invoiceOfACompletedEncounter().id();

        assertThatThrownBy(() -> billing.refund(otherInvoice, paymentId, new RefundReason("Engano")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("payment %s does not belong to invoice %s".formatted(paymentId, otherInvoice));
    }

    @Test
    void aSettledInvoiceTakesNoFurtherPayment() {
        pay(SERVICE_PRICE);

        assertThatThrownBy(() -> pay("10.00"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("an invoice in status PAID cannot be paid");
    }

    @Test
    void aDiscountMakesTheRemainingPaymentSmaller() {
        billing.applyDiscount(invoiceId, Money.of("30.00"), new DiscountReason("Cliente antigo"));

        InvoiceSnapshot settled = pay("150.00");

        assertThat(settled.status()).isEqualTo(InvoiceStatus.PAID);
    }

    private InvoiceSnapshot pay(String amount) {
        return billing.settle(invoiceId, new PaymentDetails(Money.of(amount), PaymentMethod.PIX));
    }

    private PaymentSnapshot onlyPaymentOf(InvoiceSnapshot invoice) {
        List<PaymentSnapshot> payments = invoice.payments();
        return payments.getFirst();
    }
}
