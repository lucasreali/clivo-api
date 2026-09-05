package com.example.clivoapi.core.encounter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class EncounterApiTest extends EncounterFixture {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void twoClinicsGetDifferentSheetsFromTheSameEndpoint() throws Exception {
        openClinic("TEST-API-VET");
        Tenant vet = clinic();
        Long vetEncounter = openWith("species", "SHORT_TEXT");

        openClinic("TEST-API-DENTAL");
        Tenant dental = clinic();
        Long dentalEncounter = openWith("tooth", "INTEGER");

        bindTenant(vet);
        mockMvc.perform(get("/api/encounters/{id}", vetEncounter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.templateName").value("Consultation"))
                .andExpect(jsonPath("$.sheet.sections[0].fields[0].code").value("species"))
                .andExpect(jsonPath("$.sheet.sections[0].fields[0].fieldType").value("SHORT_TEXT"));

        bindTenant(dental);
        mockMvc.perform(get("/api/encounters/{id}", dentalEncounter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.sections[0].fields[0].code").value("tooth"))
                .andExpect(jsonPath("$.sheet.sections[0].fields[0].fieldType").value("INTEGER"));
    }

    @Test
    void theRecordIsFilledInAndCompletedThroughTheApi() throws Exception {
        openClinic("TEST-API-ENCOUNTER");
        Long id = openWith("complaint", "LONG_TEXT");

        mockMvc.perform(put("/api/encounters/{id}/record", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"values\":{\"complaint\":\"Dor no dente 26\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.sections[0].fields[0].value").value("Dor no dente 26"));

        mockMvc.perform(post("/api/encounters/{id}/completion", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void openingAnEncounterWithoutATemplateIsRefused() throws Exception {
        openClinic("TEST-API-NO-TEMPLATE");

        mockMvc.perform(post("/api/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"practitionerId\":%d}"
                                .formatted(customerId(), practitionerId())))
                .andExpect(status().isBadRequest());
    }

    private Long openWith(String fieldCode, String fieldType) {
        Long templateId = publishTemplate("Consultation", complaintWith(fieldCode, fieldType));
        return encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), templateId))
                .id();
    }
}
