package com.example.clivoapi.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void businessFailureBecomesUnprocessableEntity() throws Exception {
        mockMvc.perform(get("/failures/business"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.path").value("/failures/business"))
                .andExpect(jsonPath("$.message").value("batch is expired"));
    }

    @Test
    void missingResourceBecomesNotFound() throws Exception {
        mockMvc.perform(get("/failures/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer 7 not found"));
    }

    @RestController
    static class FailingController {

        @GetMapping("/failures/business")
        void refuseByBusinessRule() {
            throw new BusinessException("batch is expired");
        }

        @GetMapping("/failures/missing")
        void refuseByMissingResource() {
            throw new ResourceNotFoundException("Customer", 7L);
        }
    }
}
