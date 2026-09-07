package com.example.clivoapi.core.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.core.access.Role;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class AttachmentApiTest extends EncounterFixture {

    private static final byte[] RADIOGRAPH = "radiografia".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    private UUID encounterId;

    @BeforeEach
    void openTheClinic() throws Exception {
        openClinic("TEST-API-ATTACHMENT");
        signIn(Role.PRACTITIONER);
        UUID templateId = publishTemplate("Anamnesis", complaintWith("complaint", "LONG_TEXT"));
        encounterId = encounters
                .open(EncounterOpening.walkIn(customerId(), practitionerId(), serviceId(), templateId))
                .id();
        encounters.fill(encounterId, RecordValues.of(Map.of("complaint", "Dor no dente 26")));
        completeAsPractitioner(encounterId);
    }

    @Test
    void aFileUploadedToAnEncounterComesBackInTheCustomerListingAndDownloads() throws Exception {
        UUID attachmentId = upload();

        mockMvc.perform(get("/api/attachments").param("customerId", customerId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("radiografia.png"))
                .andExpect(jsonPath("$[0].sizeBytes").value(RADIOGRAPH.length))
                .andExpect(jsonPath("$[0].authorName").value(Role.PRACTITIONER.name()));

        byte[] downloaded = mockMvc.perform(get("/api/attachments/{id}/content", attachmentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(downloaded).isEqualTo(RADIOGRAPH);
    }

    @Test
    void theHistoryOfTheCustomerCarriesTheUploadedFile() throws Exception {
        upload();

        mockMvc.perform(get("/api/encounters").param("customerId", customerId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounters[0].attachments[0].fileName").value("radiografia.png"));
    }

    private UUID upload() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "radiografia.png", "image/png", RADIOGRAPH);
        String body = mockMvc.perform(multipart("/api/attachments")
                        .file(file)
                        .param("encounterId", encounterId.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }
}
