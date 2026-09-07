package com.example.clivoapi.core.access;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.common.tenant.TenantIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", updatable = false)
    private Tenant clinic;

    @Column(nullable = false)
    private String name;

    @Embedded
    private EmailAddress email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppUserStatus status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUser() {
    }

    public AppUser(Tenant clinic, UserRegistration registration, HashedPassword password) {
        requireClinicMatching(registration.role(), clinic);
        this.clinic = clinic;
        this.name = registration.name();
        this.email = registration.email();
        this.passwordHash = password.asText();
        this.role = registration.role();
        this.status = AppUserStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isActive() {
        return status == AppUserStatus.ACTIVE;
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }

    public boolean canCreate(Role requested) {
        return isActive() && role.canCreate(requested);
    }

    public boolean canAssign(Role requested) {
        return isActive() && role.canAssign(requested);
    }

    public boolean canLeaveManagement(ManagementQuorum management) {
        return !role.manages() || !isActive() || management.hasAnotherManager();
    }

    public boolean managesTheClinic() {
        return isActive() && role.manages();
    }

    public boolean keepsEveryModule() {
        return role.manages();
    }

    public AppUser create(UserRegistration registration, PasswordHashing hashing) {
        requireAbleToCreate(registration.role());
        return new AppUser(clinic, registration, hashing.hash(registration.password()));
    }

    public boolean signsInWith(RawPassword password, PasswordHashing hashing) {
        return isActive() && hashing.matches(password, new HashedPassword(passwordHash));
    }

    public SignedInSession signIn() {
        lastLoginAt = Instant.now();
        return session();
    }

    public SignedInSession session() {
        return new SignedInSession(identity(), clinicIdentity().orElse(null));
    }

    public AuthenticatedUser identity() {
        return new AuthenticatedUser(id, clinicId().orElse(null), name, role);
    }

    public UserSummary summary() {
        return new UserSummary(id, name, email, role, isActive());
    }

    public void changeRoleTo(Role requested, ManagementQuorum management) {
        requireAbleToBecome(requested, management);
        role = requested;
    }

    public void deactivate(ManagementQuorum management) {
        requireAbleToLeaveManagement(management);
        status = AppUserStatus.INACTIVE;
    }

    private Optional<TenantIdentity> clinicIdentity() {
        return Optional.ofNullable(clinic).map(Tenant::identity);
    }

    private Optional<UUID> clinicId() {
        return Optional.ofNullable(clinic).map(Tenant::id);
    }

    private void requireAbleToBecome(Role requested, ManagementQuorum management) {
        if (requested.manages()) {
            return;
        }
        requireAbleToLeaveManagement(management);
    }

    private void requireAbleToLeaveManagement(ManagementQuorum management) {
        if (canLeaveManagement(management)) {
            return;
        }
        throw new BusinessException("the last manager of the clinic may not leave management");
    }

    private void requireAbleToCreate(Role requested) {
        if (canCreate(requested)) {
            return;
        }
        throw new ForbiddenOperationException("a %s may not create a %s".formatted(role, requested));
    }

    private void requireClinicMatching(Role role, Tenant clinic) {
        if (role.belongsToClinic() == (clinic != null)) {
            return;
        }
        throw new BusinessException("role %s must be registered %s".formatted(role, placeOf(role)));
    }

    private String placeOf(Role role) {
        return role.belongsToClinic() ? "within a clinic" : "without a clinic";
    }
}
