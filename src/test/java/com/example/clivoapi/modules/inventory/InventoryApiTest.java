package com.example.clivoapi.modules.inventory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class InventoryApiTest extends InventoryFixture {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aProductIsRegisteredAndReceivesItsFirstInboundMovement() throws Exception {
        openClinicWithInventory("TEST-INV-API-ON");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Anestésico\",\"unit\":\"ml\",\"minStock\":10,\"batchControlled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.onHand").value(0))
                .andExpect(jsonPath("$.belowMinimum").value(true));

        Long productId = registerGauze("5");

        mockMvc.perform(post("/api/products/%d/movements".formatted(productId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"INBOUND\",\"quantity\":12,\"reason\":\"compra mensal\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.onHand").value(12.00))
                .andExpect(jsonPath("$.belowMinimum").value(false));
    }

    @Test
    void aClinicWithoutTheModuleDoesNotEvenSeeTheEndpoint() throws Exception {
        openClinic("TEST-INV-API-OFF");

        mockMvc.perform(get("/api/products")).andExpect(status().isNotFound());
    }
}
