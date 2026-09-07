package com.example.clivoapi.common.tenant;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tenant")
public class Tenant {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(nullable = false, updatable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Embedded
    private TenantProfile profile;

    @Embedded
    private TenantLifecycle lifecycle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Tenant() {
    }

    public Tenant(String code, String name) {
        this(new TenantRegistration(code, name, TenantProfile.unknown()));
    }

    public Tenant(TenantRegistration registration) {
        this.code = registration.code();
        this.name = registration.name();
        this.profile = registration.profile();
        this.lifecycle = TenantLifecycle.opened();
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public boolean isActive() {
        return lifecycle.isActive();
    }

    public void describeAs(TenantDetails details) {
        this.name = details.name();
        this.profile = details.profile();
    }

    public void activate() {
        lifecycle = lifecycle.activated();
    }

    public void deactivate(String reason) {
        lifecycle = lifecycle.suspended(reason);
    }

    public void close(String reason) {
        lifecycle = lifecycle.closed(reason);
    }

    public TenantIdentity identity() {
        return new TenantIdentity(id, code, name);
    }

    public TenantSnapshot snapshot() {
        return new TenantSnapshot(identity(), profile, lifecycle, createdAt);
    }
}
