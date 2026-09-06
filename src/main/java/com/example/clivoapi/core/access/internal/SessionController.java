package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.tenant.TenantResolutionFilter;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SessionController.PATH)
@Tag(name = "Session", description = "Signing in and out; the session cookie every other operation needs")
class SessionController {

    static final String PATH = "/api/session";

    private final AccessService access;
    private final SecurityContextRepository contexts;

    SessionController(AccessService access, SecurityContextRepository contexts) {
        this.access = access;
        this.contexts = contexts;
    }

    @Operation(operationId = "signIn", summary = "Open a session and receive the session cookie")
    @SecurityRequirements
    @PostMapping
    SessionView signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AuthenticatedUser user = access.signIn(request.toAttempt());
        open(user, httpRequest, httpResponse);
        return SessionView.of(user);
    }

    @Operation(operationId = "getCurrentSession", summary = "Describe the user behind the current session")
    @GetMapping
    SessionView current(@AuthenticationPrincipal AuthenticatedUser user) {
        return SessionView.of(user);
    }

    @Operation(operationId = "signOut", summary = "Close the current session")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void signOut(HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false)).ifPresent(HttpSession::invalidate);
        SecurityContextHolder.clearContext();
    }

    private void open(AuthenticatedUser user, HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationOf(user));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        bindClinicTo(request.getSession(true), user);
    }

    private void bindClinicTo(HttpSession session, AuthenticatedUser user) {
        user.clinic().ifPresent(clinic ->
                session.setAttribute(TenantResolutionFilter.TENANT_SESSION_ATTRIBUTE, clinic));
    }

    private Authentication authenticationOf(AuthenticatedUser user) {
        return UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of(new SimpleGrantedAuthority(user.role().authority())));
    }
}
