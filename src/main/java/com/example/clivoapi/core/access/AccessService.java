package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.core.access.internal.AppUserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccessService {

    private final AppUserRepository users;
    private final PasswordHashing hashing;
    private final AuditorAware<UUID> auditor;
    private final TenantContext tenantContext;

    AccessService(
            AppUserRepository users,
            PasswordHashing hashing,
            AuditorAware<UUID> auditor,
            TenantContext tenantContext) {
        this.users = users;
        this.hashing = hashing;
        this.auditor = auditor;
        this.tenantContext = tenantContext;
    }

    public SignedInSession signIn(SignInAttempt attempt) {
        AppUser user = users.findByEmail(attempt.email().asText()).orElseThrow(InvalidCredentialsException::new);
        requireMatchingPassword(user, attempt.password());
        return user.signIn();
    }

    @Transactional(readOnly = true)
    public SignedInSession sessionOf(AuthenticatedUser user) {
        return users.findById(user.userId()).map(AppUser::session).orElseThrow(InvalidCredentialsException::new);
    }

    public UserSummary register(UserRegistration registration) {
        AppUser created = caller().create(registration, hashing);
        requireEmailAvailable(registration.email());
        return users.save(created).summary();
    }

    public UserSummary registerIn(Tenant clinic, UserRegistration registration) {
        requireEmailAvailable(registration.email());
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

    private AppUser caller() {
        return auditor.getCurrentAuditor()
                .flatMap(users::findById)
                .orElseThrow(() -> new ForbiddenOperationException("only a signed-in user creates another user"));
    }

    private void requireMatchingPassword(AppUser user, RawPassword password) {
        if (user.signsInWith(password, hashing)) {
            return;
        }
        throw new InvalidCredentialsException();
    }

    private void requireEmailAvailable(EmailAddress email) {
        if (users.existsByEmail(email.asText())) {
            throw new BusinessException("email %s is already registered".formatted(email));
        }
    }
}
