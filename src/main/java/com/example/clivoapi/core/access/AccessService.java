package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.common.tenant.TenantDirectory;
import com.example.clivoapi.core.access.internal.AppUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccessService {

    private final AppUserRepository users;
    private final TenantDirectory clinics;
    private final PasswordHashing hashing;
    private final TenantContext tenantContext;

    AccessService(
            AppUserRepository users,
            TenantDirectory clinics,
            PasswordHashing hashing,
            TenantContext tenantContext) {
        this.users = users;
        this.clinics = clinics;
        this.hashing = hashing;
        this.tenantContext = tenantContext;
    }

    public AuthenticatedUser signIn(SignInAttempt attempt) {
        AppUser user = locate(attempt).orElseThrow(InvalidCredentialsException::new);
        requireMatchingPassword(user, attempt.password());
        return user.signIn();
    }

    public UserSummary register(UserRegistration registration) {
        return registerIn(currentClinic().orElse(null), registration);
    }

    public UserSummary registerIn(Tenant clinic, UserRegistration registration) {
        requireEmailAvailable(clinic, registration.email());
        AppUser user = new AppUser(clinic, registration, hashing.hash(registration.password()));
        return users.save(user).summary();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> usersOfCurrentClinic() {
        return tenantContext.current().stream()
                .flatMap(clinic -> users.findByClinicIdOrderByNameAsc(clinic).stream())
                .map(AppUser::summary)
                .toList();
    }

    private Optional<AppUser> locate(SignInAttempt attempt) {
        String email = attempt.email().asText();
        return attempt.clinic()
                .map(code -> users.findByClinicCodeAndEmail(code, email))
                .orElseGet(() -> users.findByClinicIsNullAndEmail(email));
    }

    private void requireMatchingPassword(AppUser user, RawPassword password) {
        if (user.signsInWith(password, hashing)) {
            return;
        }
        throw new InvalidCredentialsException();
    }

    private void requireEmailAvailable(Tenant clinic, EmailAddress email) {
        if (isTaken(clinic, email)) {
            throw new BusinessException("email %s is already registered".formatted(email));
        }
    }

    private boolean isTaken(Tenant clinic, EmailAddress email) {
        return Optional.ofNullable(clinic)
                .map(existing -> users.existsByClinicIdAndEmail(existing.id(), email.asText()))
                .orElseGet(() -> users.findByClinicIsNullAndEmail(email.asText()).isPresent());
    }

    private Optional<Tenant> currentClinic() {
        return clinics.current();
    }
}
