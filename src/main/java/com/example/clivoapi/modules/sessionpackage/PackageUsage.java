package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "package_usage")
public class PackageUsage extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_package_id", nullable = false, updatable = false)
    private SessionPackage sessionPackage;

    @Column(name = "encounter_id", nullable = false, updatable = false)
    private Long encounterId;

    @Column(name = "used_at", nullable = false, updatable = false)
    private Instant usedAt;

    protected PackageUsage() {
    }

    PackageUsage(SessionPackage sessionPackage, Long encounterId) {
        this.sessionPackage = sessionPackage;
        this.encounterId = encounterId;
        this.usedAt = Instant.now();
    }

    boolean records(Long otherEncounterId) {
        return encounterId.equals(otherEncounterId);
    }
}
