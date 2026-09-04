package com.example.clivoapi.core.catalog;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "service")
public class Service extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private ServiceDuration duration;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false, precision = 10, scale = 2))
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    protected Service() {
    }

    public Service(ServiceDetails details) {
        this.status = ServiceStatus.ACTIVE;
        describeAs(details);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isActive() {
        return status == ServiceStatus.ACTIVE;
    }

    public LocalDateTime endTimeFrom(LocalDateTime start) {
        return duration.endFrom(start);
    }

    public Money price() {
        return price;
    }

    public void describeAs(ServiceDetails details) {
        this.name = details.name();
        this.duration = details.duration();
        this.price = details.price();
    }

    public void deactivate() {
        status = ServiceStatus.INACTIVE;
    }

    public ServiceSnapshot snapshot() {
        return new ServiceSnapshot(id, new ServiceDetails(name, duration, price), status);
    }
}
