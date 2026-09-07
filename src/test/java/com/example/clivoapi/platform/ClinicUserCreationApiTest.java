package com.example.clivoapi.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
class ClinicUserCreationApiTest extends DatabaseTest {

    private static final String PASSWORD = "open-sesame";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessService access;

    @Test
    void aManagerDoesNotPromoteAnyoneToPlatformAdministrator() throws Exception {
        Tenant clinic = createTenant("TEST-NO-CLIMB");
        register(clinic, "carla@climb.test", Role.MANAGER);

        mockMvc.perform(newUserOf("root@climb.test", "PLATFORM_ADMIN", null).session(signIn("carla@climb.test")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("a MANAGER may not create a PLATFORM_ADMIN"));

        assertThat(usersNamed("root@climb.test")).isZero();
    }

    @Test
    void aManagerCreatesUsersInTheirOwnClinicOnly() throws Exception {
        Tenant clinic = createTenant("TEST-OWN-HOUSE");
        Tenant other = createTenant("TEST-OTHER-HOUSE");
        register(clinic, "carla@own.test", Role.MANAGER);

        mockMvc.perform(newUserOf("dora@own.test", "ASSISTANT", other.id()).session(signIn("carla@own.test")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("dora@own.test"));

        assertThat(clinicOf("dora@own.test")).isEqualTo(clinic.id());
    }

    @Test
    void noOtherClinicRoleCreatesAUser() throws Exception {
        Tenant clinic = createTenant("TEST-NO-HANDS");
        register(clinic, "rita@hands.test", Role.RECEPTION);
        register(clinic, "pedro@hands.test", Role.PRACTITIONER);
        register(clinic, "alice@hands.test", Role.ASSISTANT);

        for (String email : new String[] {"rita@hands.test", "pedro@hands.test", "alice@hands.test"}) {
            mockMvc.perform(newUserOf("dora@hands.test", "ASSISTANT", null).session(signIn(email)))
                    .andExpect(status().isForbidden());
        }

        assertThat(usersNamed("dora@hands.test")).isZero();
    }

    @Test
    void aPlatformAdministratorIsRegisteredOnThePlatformEndpoint() throws Exception {
        access.registerIn(
                null,
                new UserRegistration(
                        "Root",
                        new EmailAddress("root@platform.test"),
                        new RawPassword(PASSWORD),
                        Role.PLATFORM_ADMIN));

        mockMvc.perform(post("/api/platform/administrators")
                        .session(signIn("root@platform.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ivo\",\"email\":\"ivo@platform.test\",\"password\":\"%s\"}"
                                .formatted(PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PLATFORM_ADMIN"));

        assertThat(usersNamed("ivo@platform.test")).isOne();
        assertThat(clinicOf("ivo@platform.test")).isNull();
    }

    private MockHttpServletRequestBuilder newUserOf(String email, String role, UUID clinicId) {
        return post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name":"Dora","email":"%s","password":"%s","role":"%s",\
                        "clinicId":"%s","tenantId":"%s"}"""
                                .formatted(email, PASSWORD, role, clinicId, clinicId));
    }

    private MockHttpSession signIn(String email) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private void register(Tenant clinic, String email, Role role) {
        access.registerIn(
                clinic, new UserRegistration("Carla", new EmailAddress(email), new RawPassword(PASSWORD), role));
    }

    private int usersNamed(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM app_user WHERE email = ?", Integer.class, email);
    }

    private UUID clinicOf(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT tenant_id FROM app_user WHERE email = ?", UUID.class, email);
    }
}
