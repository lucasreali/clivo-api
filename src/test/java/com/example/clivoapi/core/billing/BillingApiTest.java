package com.example.clivoapi.core.billing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class BillingApiTest extends BillingFixture {

    @Autowired
    private MockMvc mockMvc;

    private Long invoiceId;

    @BeforeEach
    void openTheClinic() {
        openClinic("TEST-BILLING-API");
        invoiceId = invoiceOfACompletedEncounter().id();
    }

    @Test
    void theCustomerInvoicesAreListedWithTheirBalance() throws Exception {
        mockMvc.perform(get("/api/invoices").param("customerId", customerId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].coverage").value("DIRECT"))
                .andExpect(jsonPath("$[0].netAmount").value(180.00))
                .andExpect(jsonPath("$[0].outstandingBalance").value(180.00))
                .andExpect(jsonPath("$[0].lines[0].description").value("Limpeza"));
    }

    @Test
    void aPartialPaymentIsRecordedAndLeavesTheInvoicePartial() throws Exception {
        mockMvc.perform(post("/api/invoices/{id}/payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":80.00,\"method\":\"PIX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.outstandingBalance").value(100.00))
                .andExpect(jsonPath("$.payments[0].method").value("PIX"))
                .andExpect(jsonPath("$.payments[0].refunded").value(false));
    }

    @Test
    void aPaymentAboveTheBalanceIsRefusedWithTheBusinessStatus() throws Exception {
        mockMvc.perform(post("/api/invoices/{id}/payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":900.00,\"method\":\"CASH\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void aDiscountWithoutAReasonIsRefusedWithTheBusinessStatus() throws Exception {
        mockMvc.perform(post("/api/invoices/{id}/discount", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void aRefundPutsTheInvoiceBackInTheOpenState() throws Exception {
        PaymentDetails inFull = new PaymentDetails(Money.of(SERVICE_PRICE), PaymentMethod.CASH);
        Long paymentId = billing.settle(invoiceId, inFull).payments().getFirst().id();

        mockMvc.perform(post("/api/invoices/{id}/payments/{paymentId}/refund", invoiceId, paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Procedimento nao realizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.outstandingBalance").value(180.00))
                .andExpect(jsonPath("$.payments[0].refunded").value(true));
    }
}
