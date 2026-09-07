package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.common.tenant.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

class PlatformClinicScopeInterceptor implements HandlerInterceptor {

    private final TenantService clinics;
    private final TenantContext tenantContext;

    PlatformClinicScopeInterceptor(TenantService clinics, TenantContext tenantContext) {
        this.clinics = clinics;
        this.tenantContext = tenantContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        targetOf(request).map(clinics::findOne).map(Tenant::id).ifPresent(tenantContext::bind);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception failure) {
        tenantContext.clear();
    }

    private Optional<UUID> targetOf(HttpServletRequest request) {
        return declaredVariablesOf(request)
                .map(variables -> variables.get(PlatformPath.CLINIC_VARIABLE))
                .map(identifier -> identifierOf(identifier, request));
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, String>> declaredVariablesOf(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .map(variables -> (Map<String, String>) variables);
    }

    private UUID identifierOf(String identifier, HttpServletRequest request) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException notAnIdentifier) {
            throw new ResourceNotFoundException("resource", request.getRequestURI());
        }
    }
}
