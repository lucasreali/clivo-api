package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.tenant.TenantIdentity;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.SignedInSession;
import java.util.UUID;

record SessionView(UUID userId, String name, String role, SignedInClinicView clinic) {

    static SessionView of(SignedInSession session) {
        AuthenticatedUser user = session.user();
        return new SessionView(
                user.userId(),
                user.name(),
                user.role().name(),
                session.clinicIdentity().map(SignedInClinicView::of).orElse(null));
    }

    record SignedInClinicView(UUID id, String name) {

        static SignedInClinicView of(TenantIdentity clinic) {
            return new SignedInClinicView(clinic.id(), clinic.name());
        }
    }
}
