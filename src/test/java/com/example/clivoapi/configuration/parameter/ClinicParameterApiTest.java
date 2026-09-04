package com.example.clivoapi.configuration.parameter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class ClinicParameterApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void valueOutOfRangeAnswersUnprocessableContentWithTheLimits() throws Exception {
        bindTenant(createTenant("TEST-API-PARAM"));

        mockMvc.perform(changeOf("reminder_lead_hours", "12")).andExpect(status().isNoContent());

        mockMvc.perform(changeOf("reminder_lead_hours", "200"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("parameter reminder_lead_hours accepts a whole number between 1 and 72"));

        mockMvc.perform(get("/api/parameters"))
                .andExpect(jsonPath("$[?(@.code=='reminder_lead_hours')].value").value("12"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder changeOf(
            String code, String value) {
        return put("/api/parameters/{code}", code)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"%s\"}".formatted(value));
    }
}
