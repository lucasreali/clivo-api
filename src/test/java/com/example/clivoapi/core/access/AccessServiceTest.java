package com.example.clivoapi.core.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantIdentity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AccessServiceTest extends DatabaseTest {

    private static final RawPassword PASSWORD = new RawPassword("open-sesame");

    @Autowired
    private AccessService access;

    @Test
    void signInCarriesIdentityClinicAndRole() {
        Tenant clinic = createTenant("TEST-ACCESS");
        access.registerIn(clinic, registrationOf("ana@clivo.test", Role.MANAGER));

        SignedInSession session = access.signIn(attemptOf("ana@clivo.test", PASSWORD));

        assertThat(session.clinicIdentity()).map(TenantIdentity::id).contains(clinic.id());
        assertThat(session.user().hasRole(Role.MANAGER)).isTrue();
        assertThat(session.user().name()).isEqualTo("Ana");
    }

    @Test
    void aPlatformAdministratorSignsInWithoutAClinic() {
        access.registerIn(null, registrationOf("root@clivo.test", Role.PLATFORM_ADMIN));

        SignedInSession session = access.signIn(attemptOf("root@clivo.test", PASSWORD));

        assertThat(session.clinicIdentity()).isEmpty();
    }

    @Test
    void aClinicRoleWithoutAClinicIsRefused() {
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> access.registerIn(null, registrationOf("ghost@clivo.test", Role.RECEPTION)))
                .withMessageContaining("must be registered within a clinic");
    }

    @Test
    void aWrongPasswordIsRefused() {
        Tenant clinic = createTenant("TEST-ACCESS");
        access.registerIn(clinic, registrationOf("ana@clivo.test", Role.RECEPTION));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> access.signIn(attemptOf("ana@clivo.test", new RawPassword("wrong-guess"))));
    }

    @Test
    void anInactiveUserIsRefused() {
        Tenant clinic = createTenant("TEST-ACCESS");
        UserSummary registered = access.registerIn(clinic, registrationOf("ana@clivo.test", Role.RECEPTION));
        deactivate(registered.id());

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> access.signIn(attemptOf("ana@clivo.test", PASSWORD)));
    }

    @Test
    void anEmailIdentifiesOneUserAcrossEveryClinic() {
        Tenant north = createTenant("TEST-NORTH");
        Tenant south = createTenant("TEST-SOUTH");
        access.registerIn(north, registrationOf("ana@clivo.test", Role.RECEPTION));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> access.registerIn(south, registrationOf("ana@clivo.test", Role.RECEPTION)))
                .withMessageContaining("already registered");
    }

    private void deactivate(UUID userId) {
        jdbcTemplate.update("UPDATE app_user SET status = 'INACTIVE' WHERE id = ?", userId);
    }

    private UserRegistration registrationOf(String email, Role role) {
        return new UserRegistration("Ana", new EmailAddress(email), PASSWORD, role);
    }

    private SignInAttempt attemptOf(String email, RawPassword password) {
        return new SignInAttempt(new EmailAddress(email), password);
    }
}
