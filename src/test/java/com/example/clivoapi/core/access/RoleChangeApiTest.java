package com.example.clivoapi.core.access;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.tenant.Tenant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc(addFilters = false)
class RoleChangeApiTest extends DatabaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aManagerPromotesAUserToManager() throws Exception {
        Tenant clinic = openClinic("TEST-ROLE-PROMOTE");
        UUID reception = signInAs(clinic, Role.RECEPTION).id();
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(roleOf(reception, "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void aManagerNeverAssignsThePlatformAdministratorRole() throws Exception {
        Tenant clinic = openClinic("TEST-ROLE-ESCALATION");
        UUID reception = signInAs(clinic, Role.RECEPTION).id();
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(roleOf(reception, "PLATFORM_ADMIN"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("a user may not assign the role PLATFORM_ADMIN"));
    }

    @Test
    void aUserOfAnotherClinicIsNotFound() throws Exception {
        Tenant other = openClinic("TEST-ROLE-OTHER");
        UUID stranger = signInAs(other, Role.RECEPTION).id();

        Tenant clinic = openClinic("TEST-ROLE-MINE");
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(roleOf(stranger, "ASSISTANT")).andExpect(status().isNotFound());
    }

    @Test
    void theLastManagerOfAClinicIsNeitherDemotedNorDeactivated() throws Exception {
        Tenant clinic = openClinic("TEST-ROLE-LAST");
        UUID manager = signInAs(clinic, Role.MANAGER).id();

        mockMvc.perform(roleOf(manager, "ASSISTANT"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("the last manager of the clinic may not leave management"));

        mockMvc.perform(post("/api/users/%s/deactivation".formatted(manager)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void aManagerStepsDownOnceAnotherManagerIsInPlace() throws Exception {
        Tenant clinic = openClinic("TEST-ROLE-HANDOVER");
        UUID reception = signInAs(clinic, Role.RECEPTION).id();
        UUID manager = signInAs(clinic, Role.MANAGER).id();

        mockMvc.perform(roleOf(reception, "MANAGER")).andExpect(status().isOk());
        mockMvc.perform(roleOf(manager, "ASSISTANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ASSISTANT"));
    }

    @Test
    void aReceptionUserNeverChangesARole() throws Exception {
        Tenant clinic = openClinic("TEST-ROLE-RECEPTION");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();
        signInAs(clinic, Role.RECEPTION);

        mockMvc.perform(roleOf(assistant, "MANAGER")).andExpect(status().isForbidden());
    }

    private Tenant openClinic(String name) {
        Tenant clinic = createTenant(name);
        bindTenant(clinic);
        return clinic;
    }

    private MockHttpServletRequestBuilder roleOf(UUID userId, String role) {
        return put("/api/users/%s/role".formatted(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"%s\"}".formatted(role));
    }
}
