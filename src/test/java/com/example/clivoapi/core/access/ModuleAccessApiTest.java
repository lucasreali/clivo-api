package com.example.clivoapi.core.access;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
class ModuleAccessApiTest extends DatabaseTest {

    private static final ModuleCode INVENTORY = new ModuleCode("inventory");

    private static final String PRODUCTS = "/api/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModuleActivationService modules;

    @Test
    void aUserGrantedTheModuleReachesIt() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-REACH");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();

        signInAs(clinic, Role.MANAGER);
        mockMvc.perform(put(grantOf(assistant, "inventory")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("inventory"));

        authenticate(assistant, clinic.id(), "ASSISTANT", Role.ASSISTANT);
        mockMvc.perform(get(PRODUCTS)).andExpect(status().isOk());
    }

    @Test
    void theSameUserWithoutTheGrantIsForbidden() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-MISSING");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();

        authenticate(assistant, clinic.id(), "ASSISTANT", Role.ASSISTANT);
        mockMvc.perform(get(PRODUCTS))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("access to module inventory was not granted"));
    }

    @Test
    void aModuleTheClinicNeverContractedIsNotFoundEvenForTheManager() throws Exception {
        Tenant clinic = createTenant("TEST-GRANT-UNCONTRACTED");
        bindTenant(clinic);
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(get(PRODUCTS)).andExpect(status().isNotFound());

        authenticate(assistant, clinic.id(), "ASSISTANT", Role.ASSISTANT);
        mockMvc.perform(get(PRODUCTS)).andExpect(status().isNotFound());
    }

    @Test
    void aManagerMayNotGrantAModuleTheClinicDoesNotHave() throws Exception {
        Tenant clinic = createTenant("TEST-GRANT-REFUSED");
        bindTenant(clinic);
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(put(grantOf(assistant, "inventory"))).andExpect(status().isNotFound());
    }

    @Test
    void aManagerKeepsEveryModuleActiveInTheClinic() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-MANAGER");
        UUID manager = signInAs(clinic, Role.MANAGER).id();

        mockMvc.perform(delete(grantOf(manager, "inventory")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("a manager keeps access to every module active in the clinic"));

        mockMvc.perform(get(PRODUCTS)).andExpect(status().isOk());
    }

    @Test
    void aRevokedGrantClosesTheModuleAgain() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-REVOKE");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();

        signInAs(clinic, Role.MANAGER);
        mockMvc.perform(put(grantOf(assistant, "inventory"))).andExpect(status().isOk());
        mockMvc.perform(delete(grantOf(assistant, "inventory")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        authenticate(assistant, clinic.id(), "ASSISTANT", Role.ASSISTANT);
        mockMvc.perform(get(PRODUCTS)).andExpect(status().isForbidden());
    }

    @Test
    void aReceptionUserDoesNotManageGrants() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-RECEPTION");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();
        signInAs(clinic, Role.RECEPTION);

        mockMvc.perform(put(grantOf(assistant, "inventory")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("only a manager decides who reaches a module"));
    }

    @Test
    void aManagerNeverGrantsToAUserOfAnotherClinic() throws Exception {
        Tenant other = createTenant("TEST-GRANT-OTHER");
        bindTenant(other);
        UUID stranger = signInAs(other, Role.ASSISTANT).id();

        Tenant clinic = openClinicWithInventory("TEST-GRANT-MINE");
        signInAs(clinic, Role.MANAGER);

        mockMvc.perform(put(grantOf(stranger, "inventory"))).andExpect(status().isNotFound());
    }

    @Test
    void theCapabilitiesShowOnlyWhatTheCallerReaches() throws Exception {
        Tenant clinic = openClinicWithInventory("TEST-GRANT-CAPABILITIES");
        UUID assistant = signInAs(clinic, Role.ASSISTANT).id();

        signInAs(clinic, Role.MANAGER);
        mockMvc.perform(get("/api/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[?(@.code=='inventory')]").exists());

        authenticate(assistant, clinic.id(), "ASSISTANT", Role.ASSISTANT);
        mockMvc.perform(get("/api/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules").isEmpty());
    }

    private Tenant openClinicWithInventory(String name) {
        Tenant clinic = createTenant(name);
        bindTenant(clinic);
        modules.activate(INVENTORY);
        return clinic;
    }

    private String grantOf(UUID userId, String module) {
        return "/api/users/%s/modules/%s".formatted(userId, module);
    }
}
