package com.example.clivoapi.common.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MultiTenancyConfiguration {

    @Bean
    CurrentTenantIdentifierResolver<Long> currentTenantIdentifierResolver(TenantContext tenantContext) {
        return new RequestTenantIdentifierResolver(tenantContext);
    }

    @Bean
    HibernatePropertiesCustomizer tenantIdentifierResolverCustomizer(CurrentTenantIdentifierResolver<Long> resolver) {
        return properties -> properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
