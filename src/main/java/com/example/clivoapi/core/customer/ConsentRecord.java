package com.example.clivoapi.core.customer;

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
@Table(name = "consent")
public class ConsentRecord extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private boolean granted;

    @Column(nullable = false)
    private String source;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected ConsentRecord() {
    }

    ConsentRecord(Customer customer, ConsentStatement statement) {
        this.customer = customer;
        this.purpose = statement.purpose().asText();
        this.granted = statement.granted();
        this.source = statement.source();
        this.recordedAt = Instant.now();
    }

    Long id() {
        return id;
    }

    public boolean isFor(ConsentPurpose expected) {
        return purpose.equals(expected.asText());
    }

    public boolean isGranted() {
        return granted;
    }

    public Instant recordedAt() {
        return recordedAt;
    }
}
