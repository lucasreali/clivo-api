package com.example.clivoapi.common.audit;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticatedAuditor implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuditorIdentity.class::isInstance)
                .map(AuditorIdentity.class::cast)
                .map(AuditorIdentity::userId);
    }
}
