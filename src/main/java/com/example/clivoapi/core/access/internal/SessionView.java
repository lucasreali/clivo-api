package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.tenant.TenantIdentity;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.SignedInSession;
import java.util.UUID;

record SessionView(UUID userId, String name, String role, ClinicView clinic) {

    static SessionView of(SignedInSession session) {
        AuthenticatedUser user = session.user();
        return new SessionView(
                user.userId(),
                user.name(),
                user.role().name(),
                session.clinicIdentity().map(ClinicView::of).orElse(null));
    }

    record ClinicView(UUID id, String code, String name) {

        static ClinicView of(TenantIdentity clinic) {
            return new ClinicView(clinic.id(), clinic.code(), clinic.name());
        }
    }
}
