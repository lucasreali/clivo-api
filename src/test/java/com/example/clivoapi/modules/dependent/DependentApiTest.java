package com.example.clivoapi.modules.dependent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc(addFilters = false)
class DependentApiTest extends DependentFixture {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aClinicWithTheModuleRegistersADependent() throws Exception {
        Tenant clinic = openClinicWithDependents("TEST-DEP-API-ON");
        Long customerId = valueInTenant(clinic, () -> registerCustomer("Ana Prado"));
        bindTenant(clinic);

        mockMvc.perform(newDependentOf(customerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ANIMAL"))
                .andExpect(jsonPath("$.custodian").value("tutor"))
                .andExpect(jsonPath("$.attributes.species").value("dog"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void aClinicWithoutTheModuleDoesNotEvenSeeTheEndpoint() throws Exception {
        Tenant clinic = openClinic("TEST-DEP-API-OFF");
        Long customerId = valueInTenant(clinic, () -> registerCustomer("Ana Prado"));
        bindTenant(clinic);

        mockMvc.perform(get("/api/customers/%d/dependents".formatted(customerId)))
                .andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder newDependentOf(Long customerId) {
        return post("/api/customers/%d/dependents".formatted(customerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name":"Nina","type":"ANIMAL","birthDate":"2021-05-10",\
                        "attributes":{"species":"dog","breed":"beagle"}}""");
    }
}
