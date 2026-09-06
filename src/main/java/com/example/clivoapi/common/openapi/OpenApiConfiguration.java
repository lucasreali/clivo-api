package com.example.clivoapi.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    private static final String SESSION_COOKIE = "sessionCookie";

    private static final String DESCRIPTION = """
            Clinic management API. Every request runs inside the clinic bound to the caller's session,
            and reads never cross that boundary.

            Authentication is a session cookie: `POST /api/session` opens it, `DELETE /api/session` closes it,
            and every other operation requires it.

            Endpoints belonging to an optional module answer `404` while the clinic has not activated that
            module, so a client should treat their absence as a feature the clinic did not contract.
            `GET /api/capabilities` reports what is currently available.

            Errors always carry the same body: status, message, request path and, for a rejected payload,
            the offending fields.""";

    @Bean
    OpenAPI clivoApi() {
        return new OpenAPI()
                .info(clivoInfo())
                .components(new Components().addSecuritySchemes(SESSION_COOKIE, sessionCookie()))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE));
    }

    private Info clivoInfo() {
        return new Info().title("Clivo API").version("0.0.1").description(DESCRIPTION);
    }

    private SecurityScheme sessionCookie() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("Session cookie issued by POST /api/session.");
    }
}
