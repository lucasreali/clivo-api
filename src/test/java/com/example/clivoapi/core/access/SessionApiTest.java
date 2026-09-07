package com.example.clivoapi.core.access;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.tenant.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
class SessionApiTest extends DatabaseTest {

    private static final String PASSWORD = "open-sesame";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessService access;

    @Test
    void theSessionOfTheSignedInUserResolvesTheClinicOfEveryRequest() throws Exception {
        Tenant north = createTenant("TEST-NORTH");
        Tenant south = createTenant("TEST-SOUTH");
        register(north, "ana@north.test", Role.RECEPTION);
        register(south, "bruno@south.test", Role.RECEPTION);

        MockHttpSession session = signIn("TEST-NORTH", "ana@north.test", north);

        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("ana@north.test"));
    }

    @Test
    void aRequestWithoutASessionIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongPasswordIsUnauthorized() throws Exception {
        register(createTenant("TEST-NORTH"), "ana@north.test", Role.RECEPTION);

        mockMvc.perform(signInOf("TEST-NORTH", "ana@north.test", "wrong-guess"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAManagerProvisionsUsers() throws Exception {
        Tenant north = createTenant("TEST-NORTH");
        register(north, "ana@north.test", Role.RECEPTION);
        register(north, "carla@north.test", Role.MANAGER);

        mockMvc.perform(newUserOf("dora@north.test").session(signIn("TEST-NORTH", "ana@north.test", north)))
                .andExpect(status().isForbidden());

        mockMvc.perform(newUserOf("dora@north.test").session(signIn("TEST-NORTH", "carla@north.test", north)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("dora@north.test"));
    }

    private MockHttpServletRequestBuilder newUserOf(String email) {
        return post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Dora\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"ASSISTANT\"}"
                        .formatted(email, PASSWORD));
    }

    private MockHttpSession signIn(String clinic, String email, Tenant expected) throws Exception {
        return (MockHttpSession) mockMvc.perform(signInOf(clinic, email, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicId").value(expected.id().toString()))
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private MockHttpServletRequestBuilder signInOf(String clinic, String email, String password) {
        return post("/api/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clinic\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(clinic, email, password));
    }

    private void register(Tenant clinic, String email, Role role) {
        access.registerIn(clinic, new UserRegistration("Ana", new EmailAddress(email), new RawPassword(PASSWORD), role));
    }
}
