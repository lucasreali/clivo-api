package com.example.clivoapi.common.openapi;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
class RejectedPayloadResponse implements OperationCustomizer {

    private static final String ERROR_SCHEMA = "#/components/schemas/ErrorResponse";

    @Override
    public Operation customize(Operation operation, HandlerMethod endpoint) {
        if (operation.getRequestBody() == null) {
            return operation;
        }
        operation.getResponses().addApiResponse("400", rejectedPayload());
        return operation;
    }

    private ApiResponse rejectedPayload() {
        return new ApiResponse()
                .description("Bad Request")
                .content(new Content().addMediaType(APPLICATION_JSON_VALUE, errorBody()));
    }

    private MediaType errorBody() {
        return new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA));
    }
}
