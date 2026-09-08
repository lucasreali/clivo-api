package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantContext;
import com.example.clivoapi.core.access.internal.AppUserRepository;
import java.util.List;
import java.util.Optional;
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
        AppUser user = users.findByEmail(attempt.email()).orElseThrow(InvalidCredentialsException::new);
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

    public UserSummary changeRole(UUID userId, Role requested) {
        requireAbleToAssign(requested);
        AppUser user = userOfCurrentClinic(userId);
        user.changeRoleTo(requested, management());
        return users.save(user).summary();
    }

    public UserSummary deactivate(UUID userId) {
        requireManaging();
        AppUser user = userOfCurrentClinic(userId);
        user.deactivate(management());
        return users.save(user).summary();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> usersOfCurrentClinic() {
        return tenantContext.current().stream()
                .flatMap(clinic -> users.findByClinicIdOrderByNameAsc(clinic).stream())
                .map(AppUser::summary)
                .toList();
    }

    private AppUser userOfCurrentClinic(UUID userId) {
        return currentClinic()
                .flatMap(clinic -> users.findByIdAndClinicId(userId, clinic))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private ManagementQuorum management() {
        return currentClinic()
                .map(clinic -> users.countByClinicIdAndRoleAndStatus(clinic, Role.MANAGER, AppUserStatus.ACTIVE))
                .map(ManagementQuorum::new)
                .orElseGet(() -> new ManagementQuorum(0));
    }

    private Optional<UUID> currentClinic() {
        return tenantContext.current();
    }

    private void requireAbleToAssign(Role requested) {
        if (caller().canAssign(requested)) {
            return;
        }
        throw new ForbiddenOperationException("a user may not assign the role %s".formatted(requested));
    }

    private void requireManaging() {
        if (caller().managesTheClinic()) {
            return;
        }
        throw new ForbiddenOperationException("only a manager changes the users of the clinic");
    }

    @Transactional(readOnly = true)
    public AppUser signedIn() {
        return whoeverIsSignedIn()
                .orElseThrow(() -> new ForbiddenOperationException("this action requires a signed-in user"));
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> whoeverIsSignedIn() {
        return auditor.getCurrentAuditor().flatMap(users::findById);
    }

    private AppUser caller() {
        return whoeverIsSignedIn()
                .orElseThrow(() -> new ForbiddenOperationException("only a signed-in user creates another user"));
    }

    private void requireMatchingPassword(AppUser user, RawPassword password) {
        if (user.signsInWith(password, hashing)) {
            return;
        }
        throw new InvalidCredentialsException();
    }

    private void requireEmailAvailable(EmailAddress email) {
        if (users.existsByEmail(email)) {
            throw new BusinessException("email %s is already registered".formatted(email));
        }
    }
}
