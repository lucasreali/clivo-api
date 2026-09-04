package com.example.clivoapi.common.extension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.exception.ApiExceptionHandler;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class RequiresModuleInterceptorTest {

    @Test
    void clinicWithTheModuleReachesTheEndpoint() throws Exception {
        MockMvc mockMvc = apiOfClinicWith("inventory");

        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test
    void clinicWithoutTheModuleGetsNotFoundInsteadOfForbidden() throws Exception {
        MockMvc mockMvc = apiOfClinicWith("notification");

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("resource /api/products not found"));
    }

    @Test
    void endpointWithoutTheAnnotationIsNeverHidden() throws Exception {
        MockMvc mockMvc = apiOfClinicWith("notification");

        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    private MockMvc apiOfClinicWith(String activeModule) {
        Set<ModuleCode> active = Set.of(new ModuleCode(activeModule));
        return MockMvcBuilders.standaloneSetup(new ProductController(), new HealthController())
                .addInterceptors(new RequiresModuleInterceptor(active::contains))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @RestController
    @RequiresModule("inventory")
    static class ProductController {

        @GetMapping("/api/products")
        String list() {
            return "products";
        }
    }

    @RestController
    static class HealthController {

        @GetMapping("/api/health")
        String check() {
            return "healthy";
        }
    }
}
