package com.example.clivoapi.modules.sessionpackage;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.catalog.Service;
import com.example.clivoapi.core.customer.Customer;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "session_package")
public class SessionPackage extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private Service service;

    @Column(name = "total_sessions", nullable = false, updatable = false)
    private short totalSessions;

    @Column(name = "used_sessions", nullable = false)
    private short usedSessions;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false))
    private Money price;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionPackageStatus status;

    @OneToMany(mappedBy = "sessionPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageUsage> usages = new ArrayList<>();

    protected SessionPackage() {
    }

    public SessionPackage(Customer customer, Service service, PackagePurchase purchase) {
        this.customer = customer;
        this.service = service;
        this.totalSessions = purchase.totalSessions().asShort();
        this.usedSessions = 0;
        this.price = purchase.price();
        this.expiresOn = purchase.expiresOn();
        this.status = SessionPackageStatus.ACTIVE;
    }

    public UUID id() {
        return id;
    }

    public int remainingSessions() {
        return totalSessions - usedSessions;
    }

    public boolean isActive() {
        return status.isOpen() && remainingSessions() > 0 && !hasExpiredOn(LocalDate.now());
    }

    public boolean covers(UUID otherServiceId) {
        return service.id().equals(otherServiceId);
    }

    public boolean hasExpiredOn(LocalDate reference) {
        return expiresOn.isBefore(reference);
    }

    public void consumeSession(UUID encounterId) {
        requireActive();
        requireUnused(encounterId);
        usedSessions++;
        usages.add(new PackageUsage(this, encounterId));
        exhaustWhenEmpty();
    }

    public void cancel() {
        requireActive();
        status = SessionPackageStatus.CANCELLED;
    }

    public void expire() {
        status = SessionPackageStatus.EXPIRED;
    }

    public SessionPackageSnapshot snapshot() {
        return new SessionPackageSnapshot(
                id,
                customer.id(),
                service.id(),
                service.name(),
                totalSessions,
                usedSessions,
                remainingSessions(),
                price,
                expiresOn,
                status,
                isActive());
    }

    private void exhaustWhenEmpty() {
        if (remainingSessions() > 0) {
            return;
        }
        status = SessionPackageStatus.EXHAUSTED;
    }

    private void requireUnused(UUID encounterId) {
        if (usages.stream().noneMatch(usage -> usage.records(encounterId))) {
            return;
        }
        throw new BusinessException("encounter %s already consumed a session of this package".formatted(encounterId));
    }

    private void requireActive() {
        if (isActive()) {
            return;
        }
        throw new BusinessException("package %s in status %s has no session left to use".formatted(id, status));
    }
}
