package com.example.clivoapi.core.catalog;

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
class CatalogApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void bindClinic() {
        bindTenant(createTenant("TEST-CATALOG"));
    }

    @Test
    void aServiceIsRegisteredWithItsDurationAndPrice() throws Exception {
        mockMvc.perform(newServiceOf("Limpeza", 45, "180.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.price").value(180.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void aDurationOutsideTheAllowedRangeIsRefused() throws Exception {
        mockMvc.perform(newServiceOf("Cirurgia", 600, "900.00"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("a service lasts between 5 and 480 minutes"));
    }

    private MockHttpServletRequestBuilder newServiceOf(String name, int minutes, String price) {
        return post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"%s\",\"durationMinutes\":%d,\"price\":%s}".formatted(name, minutes, price));
    }
}
