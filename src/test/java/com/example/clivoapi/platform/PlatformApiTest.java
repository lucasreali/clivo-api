package com.example.clivoapi.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class PlatformApiTest extends DatabaseTest {

    private static final String PASSWORD = "open-sesame";

    private static final String ADMINISTRATOR = "root@clivo.test";

    private static final String TAX_ID = "11222333000181";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessService access;

    @Test
    void aClinicOpensWithItsFirstManagerAndNoModule() throws Exception {
        MockHttpSession session = signInAsAdministrator();

        UUID clinicId = provision(session, "TEST-OPEN", "carla@open.test");

        assertThat(managerEmailOf(clinicId)).isEqualTo("carla@open.test");
        assertThat(activationsOf(clinicId)).isZero();
        mockMvc.perform(get("/api/platform/tenants/%s/modules".formatted(clinicId)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.active==true)]").isEmpty());
    }

    @Test
    void aClinicOpensWithoutAnyIdentifierOfItsOwn() throws Exception {
        MockHttpSession session = signInAsAdministrator();

        mockMvc.perform(newClinicOf("TEST-PLAIN", "carla@plain.test").session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clinic.id").isNotEmpty())
                .andExpect(jsonPath("$.clinic.code").doesNotExist());
    }

    @Test
    void oneTaxIdBelongsToOneClinicOnly() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        mockMvc.perform(newClinicOf("TEST-HOLDER", "carla@holder.test", TAX_ID).session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clinic.taxId").value(TAX_ID));

        mockMvc.perform(newClinicOf("TEST-CLAIMER", "dora@claimer.test", TAX_ID).session(session))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("taxId: %s already belongs to TEST-HOLDER".formatted(TAX_ID)));
    }

    @Test
    void theClinicIdentifierSurvivesAnUpdateOfTheDetails() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        UUID clinicId = provision(session, "TEST-RENAME", "carla@rename.test");

        mockMvc.perform(put("/api/platform/tenants/%s".formatted(clinicId))
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clinicId.toString()))
                .andExpect(jsonPath("$.name").value("Renamed"));
    }

    @Test
    void theAdministratorDrivesTheModulesOfOneClinicThroughThePath() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        UUID clinicId = provision(session, "TEST-DRIVE", "carla@drive.test");

        mockMvc.perform(put("/api/platform/tenants/%s/modules/inventory/activation".formatted(clinicId))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/platform/tenants/%s/modules".formatted(clinicId)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='inventory')].active").value(true));
    }

    @Test
    void aModuleIsNeverActivatedBeforeTheOneItDependsOn() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        UUID clinicId = provision(session, "TEST-DEPENDENCY", "carla@dependency.test");

        mockMvc.perform(put("/api/platform/tenants/%s/modules/batch/activation".formatted(clinicId))
                        .session(session))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("module batch requires module inventory to be active"));

        mockMvc.perform(get("/api/platform/tenants/%s/modules".formatted(clinicId)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='batch')].requiresModule").value("inventory"));
    }

    @Test
    void theActivationHistoryOfOneClinicNamesTheModuleTheActionAndTheAuthor() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        UUID clinicId = provision(session, "TEST-HISTORY", "carla@history.test");

        mockMvc.perform(put("/api/platform/tenants/%s/modules/inventory/activation".formatted(clinicId))
                        .session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/platform/tenants/%s/modules/inventory/activation".formatted(clinicId))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/platform/tenants/%s/modules/history".formatted(clinicId)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("inventory"))
                .andExpect(jsonPath("$[0].action").value("DEACTIVATION"))
                .andExpect(jsonPath("$[0].author").value(administratorId().toString()))
                .andExpect(jsonPath("$[1].action").value("ACTIVATION"));
    }

    @Test
    void theAdministratorDrivesTheParametersOfOneClinicThroughThePath() throws Exception {
        MockHttpSession session = signInAsAdministrator();
        UUID clinicId = provision(session, "TEST-TUNE", "carla@tune.test");

        mockMvc.perform(put("/api/platform/tenants/%s/parameters/default_duration".formatted(clinicId))
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"45\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/platform/tenants/%s/parameters".formatted(clinicId)).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='default_duration')].value").value("45"));
    }

    @Test
    void aClinicUserFindsNoPlatformEndpoint() throws Exception {
        Tenant clinic = createTenant("TEST-CLINIC-EYES");
        register(clinic, "carla@clinic.test", Role.MANAGER);
        MockHttpSession session = signIn("carla@clinic.test");

        mockMvc.perform(get("/api/platform/tenants").session(session)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/platform/tenants/%s/modules".formatted(clinic.id())).session(session))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/platform/administrators")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Root\",\"email\":\"other@clivo.test\",\"password\":\"%s\"}"
                                .formatted(PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void aClinicUserReachingAPlatformPathChangesNoTenant() throws Exception {
        Tenant clinic = createTenant("TEST-STAYS-HOME");
        Tenant other = createTenant("TEST-ELSEWHERE");
        register(clinic, "carla@stays.test", Role.MANAGER);
        MockHttpSession session = signIn("carla@stays.test");

        mockMvc.perform(get("/api/platform/tenants/%s/modules".formatted(other.id())).session(session))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("carla@stays.test"));
    }

    private UUID provision(MockHttpSession session, String name, String managerEmail) throws Exception {
        mockMvc.perform(newClinicOf(name, managerEmail).session(session))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clinic.name").value(name))
                .andExpect(jsonPath("$.manager.role").value("MANAGER"))
                .andExpect(jsonPath("$.manager.email").value(managerEmail));
        return jdbcTemplate.queryForObject("SELECT id FROM tenant WHERE name = ?", UUID.class, name);
    }

    private MockHttpServletRequestBuilder newClinicOf(String name, String managerEmail) {
        return post("/api/platform/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name":"%s","managerName":"Carla",\
                        "managerEmail":"%s","managerPassword":"%s"}"""
                                .formatted(name, managerEmail, PASSWORD));
    }

    private MockHttpServletRequestBuilder newClinicOf(String name, String managerEmail, String taxId) {
        return post("/api/platform/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name":"%s","taxId":"%s","managerName":"Carla",\
                        "managerEmail":"%s","managerPassword":"%s"}"""
                                .formatted(name, taxId, managerEmail, PASSWORD));
    }

    private MockHttpSession signInAsAdministrator() throws Exception {
        access.registerIn(
                null,
                new UserRegistration(
                        "Root", new EmailAddress(ADMINISTRATOR), new RawPassword(PASSWORD), Role.PLATFORM_ADMIN));
        return signIn(ADMINISTRATOR);
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

    private UUID administratorId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM app_user WHERE email = ?", UUID.class, ADMINISTRATOR);
    }

    private String managerEmailOf(UUID clinicId) {
        return jdbcTemplate.queryForObject(
                "SELECT email FROM app_user WHERE tenant_id = ? AND user_role = 'MANAGER'", String.class, clinicId);
    }

    private int activationsOf(UUID clinicId) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tenant_module WHERE tenant_id = ?", Integer.class, clinicId);
    }
}
