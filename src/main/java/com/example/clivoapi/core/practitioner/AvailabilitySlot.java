package com.example.clivoapi.core.practitioner;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "availability")
public class AvailabilitySlot extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @Column(nullable = false)
    private short weekday;

    @Embedded
    private TimeRange hours;

    protected AvailabilitySlot() {
    }

    AvailabilitySlot(Practitioner practitioner, AvailabilityPeriod period) {
        this.practitioner = practitioner;
        this.weekday = period.weekday().value();
        this.hours = period.hours();
    }

    public AvailabilityPeriod period() {
        return new AvailabilityPeriod(new Weekday(weekday), hours);
    }
}
