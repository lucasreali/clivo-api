package com.example.clivoapi.core.practitioner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class PractitionerApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    private Long practitionerId;

    @BeforeEach
    void registerPractitioner() throws Exception {
        bindTenant(createTenant("TEST-PRACT"));
        practitionerId = Long.valueOf(mockMvc.perform(post("/api/practitioners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dr. Marina\",\"licenseNumber\":\"CRO-12345\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void theWeeklyScheduleIsDefinedAndQueried() throws Exception {
        mockMvc.perform(availabilityOf("""
                        {"periods":[{"weekday":"MONDAY","start":"08:00","end":"12:00"},
                                    {"weekday":"MONDAY","start":"13:00","end":"18:00"}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availability.length()").value(2));

        mockMvc.perform(get("/api/practitioners/{id}/attendance", practitionerId)
                        .param("weekday", "MONDAY")
                        .param("time", "09:30"))
                .andExpect(jsonPath("$.works").value(true));

        mockMvc.perform(get("/api/practitioners/{id}/attendance", practitionerId)
                        .param("weekday", "MONDAY")
                        .param("time", "12:30"))
                .andExpect(jsonPath("$.works").value(false));
    }

    @Test
    void anOverlappingScheduleIsRefused() throws Exception {
        mockMvc.perform(availabilityOf("""
                        {"periods":[{"weekday":"MONDAY","start":"08:00","end":"12:00"},
                                    {"weekday":"MONDAY","start":"11:00","end":"15:00"}]}"""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("availability MONDAY 08:00-12:00 overlaps another period of the same practitioner"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder availabilityOf(String body) {
        return put("/api/practitioners/{id}/availability", practitionerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
