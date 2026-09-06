package com.example.clivoapi.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.clivoapi.common.DatabaseTest;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfigureMockMvc(addFilters = false)
class OpenApiSpecificationTest extends DatabaseTest {

    private static final Path SPECIFICATION = Path.of("openapi.json");

    private static final String OWN_PACKAGE = "com.example.clivoapi";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerMapping endpoints;

    @Test
    void everyEndpointNamesTheOperationTheClientWillBeGeneratedFrom() {
        assertThat(endpointsWithoutAnOperationId())
                .as("a client generator derives its function names from operationId, "
                        + "and an endpoint that declares none is named after the Java method, "
                        + "so a second findOne becomes findOne_1")
                .isEmpty();
    }

    @Test
    void noTwoEndpointsAnswerToTheSameOperationName() {
        assertThat(operationIds()).doesNotHaveDuplicates();
    }

    @Test
    void writesTheSpecificationTheFrontendGeneratesFrom() throws Exception {
        String specification = published();

        assertThat(specification).contains("\"title\" : \"Clivo API\"", "\"registerCustomer\"");

        Files.writeString(SPECIFICATION, specification + System.lineSeparator());
    }

    private String published() throws Exception {
        return mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private List<String> endpointsWithoutAnOperationId() {
        return ourEndpoints().stream()
                .filter(endpoint -> operationIdOf(endpoint).isEmpty())
                .map(HandlerMethod::getShortLogMessage)
                .toList();
    }

    private List<String> operationIds() {
        return ourEndpoints().stream().flatMap(endpoint -> operationIdOf(endpoint).stream()).toList();
    }

    private Optional<String> operationIdOf(HandlerMethod endpoint) {
        return Optional.ofNullable(
                        AnnotatedElementUtils.findMergedAnnotation(endpoint.getMethod(), Operation.class))
                .map(Operation::operationId)
                .filter(id -> !id.isBlank());
    }

    private List<HandlerMethod> ourEndpoints() {
        return endpoints.getHandlerMethods().values().stream()
                .filter(endpoint -> endpoint.getBeanType().getPackageName().startsWith(OWN_PACKAGE))
                .toList();
    }
}
