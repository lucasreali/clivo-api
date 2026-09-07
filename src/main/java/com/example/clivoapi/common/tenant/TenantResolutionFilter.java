package com.example.clivoapi.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    private final TenantContext tenantContext;

    TenantResolutionFilter(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        bindTenantOfAuthenticatedPrincipal();
        try {
            chain.doFilter(request, response);
        } finally {
            tenantContext.clear();
        }
    }

    private void bindTenantOfAuthenticatedPrincipal() {
        authenticatedPrincipal()
                .flatMap(TenantBoundPrincipal::tenant)
                .ifPresent(tenantContext::bind);
    }

    private Optional<TenantBoundPrincipal> authenticatedPrincipal() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(TenantBoundPrincipal.class::isInstance)
                .map(TenantBoundPrincipal.class::cast);
    }
}
