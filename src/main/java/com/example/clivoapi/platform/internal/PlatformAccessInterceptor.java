package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

class PlatformAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPlatformAdministrator()) {
            return true;
        }
        throw new ResourceNotFoundException("resource", request.getRequestURI());
    }

    private boolean isPlatformAdministrator() {
        return signedInUser().filter(user -> user.hasRole(Role.PLATFORM_ADMIN)).isPresent();
    }

    private Optional<AuthenticatedUser> signedInUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);
    }
}
