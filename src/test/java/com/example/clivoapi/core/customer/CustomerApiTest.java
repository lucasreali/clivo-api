package com.example.clivoapi.core.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc(addFilters = false)
class CustomerApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void bindClinic() {
        bindTenant(createTenant("TEST-CUSTOMER"));
    }

    @Test
    void aCustomerIsRegisteredSearchedAndDeactivatedWithAReason() throws Exception {
        String id = mockMvc.perform(newCustomer())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/customers").param("name", "souza"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(deactivationOf(id, ""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("a reason is required to deactivate a customer"));

        mockMvc.perform(deactivationOf(id, "moved to another city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.deactivationReason").value("moved to another city"));
    }

    @Test
    void aConsentTurnsTheCustomerIntoAConsentedOne() throws Exception {
        String id = mockMvc.perform(newCustomer())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/customers/{id}/consents", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purpose\":\"data_processing\",\"granted\":true,\"source\":\"RECEPTION_DESK\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consented").value(true));
    }

    private MockHttpServletRequestBuilder newCustomer() {
        return post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Ana Souza","nationalId":"123.456.789-01","birthDate":"1990-05-12",
                         "phone":"41999990000","email":"ana@clivo.test",
                         "postalCode":"80000000","street":"Rua das Flores, 100"}""");
    }

    private MockHttpServletRequestBuilder deactivationOf(String id, String reason) {
        return post("/api/customers/{id}/deactivation", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"%s\"}".formatted(reason));
    }
}
