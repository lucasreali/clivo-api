package com.example.clivoapi.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    public static final String TENANT_SESSION_ATTRIBUTE = "tenantId";

    private final TenantContext tenantContext;

    TenantResolutionFilter(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        bindTenantOf(request.getSession(false));
        try {
            chain.doFilter(request, response);
        } finally {
            tenantContext.clear();
        }
    }

    private void bindTenantOf(HttpSession session) {
        if (session == null) {
            return;
        }
        Object tenantId = session.getAttribute(TENANT_SESSION_ATTRIBUTE);
        if (tenantId instanceof Long identifier) {
            tenantContext.bind(identifier);
        }
    }
}
