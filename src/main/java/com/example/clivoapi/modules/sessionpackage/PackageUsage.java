package com.example.clivoapi.modules.sessionpackage;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "package_usage")
public class PackageUsage extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_package_id", nullable = false, updatable = false)
    private SessionPackage sessionPackage;

    @Column(name = "encounter_id", nullable = false, updatable = false)
    private UUID encounterId;

    @Column(name = "used_at", nullable = false, updatable = false)
    private Instant usedAt;

    protected PackageUsage() {
    }

    PackageUsage(SessionPackage sessionPackage, UUID encounterId) {
        this.sessionPackage = sessionPackage;
        this.encounterId = encounterId;
        this.usedAt = Instant.now();
    }

    boolean records(UUID otherEncounterId) {
        return encounterId.equals(otherEncounterId);
    }
}
