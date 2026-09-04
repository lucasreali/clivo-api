package com.example.clivoapi.common.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class AuditingConfiguration {

    @Bean
    AuditorAware<Long> auditorAware() {
        return new AuthenticatedAuditor();
    }
}
