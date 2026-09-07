package com.example.clivoapi.platform.internal;

import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.common.tenant.TenantService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class PlatformWebConfiguration implements WebMvcConfigurer {

    private final TenantService clinics;
    private final TenantContext tenantContext;

    PlatformWebConfiguration(TenantService clinics, TenantContext tenantContext) {
        this.clinics = clinics;
        this.tenantContext = tenantContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PlatformAccessInterceptor())
                .addPathPatterns(PlatformPath.ROOT + "/**")
                .order(0);
        registry.addInterceptor(new PlatformClinicScopeInterceptor(clinics, tenantContext))
                .addPathPatterns(PlatformPath.ONE_CLINIC, PlatformPath.ONE_CLINIC + "/**")
                .order(1);
    }
}
