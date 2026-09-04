package com.example.clivoapi.configuration.modules;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class ModuleActivationApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void activatingBatchWithoutInventoryAnswersUnprocessableContent() throws Exception {
        bindTenant(createTenant("TEST-API-BATCH"));

        mockMvc.perform(put("/api/modules/batch/activation"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("module batch requires module inventory to be active"));
    }

    @Test
    void activationAndDeactivationAnswerNoContent() throws Exception {
        bindTenant(createTenant("TEST-API-INVENTORY"));

        mockMvc.perform(put("/api/modules/inventory/activation")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/modules/inventory/activation")).andExpect(status().isNoContent());
    }

    @Test
    void catalogIsListedWithActivationStatus() throws Exception {
        Tenant clinic = createTenant("TEST-API-CATALOG");
        bindTenant(clinic);

        mockMvc.perform(put("/api/modules/inventory/activation")).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='inventory')].active").value(true))
                .andExpect(jsonPath("$[?(@.code=='batch')].requiresModule").value("inventory"));
    }
}
