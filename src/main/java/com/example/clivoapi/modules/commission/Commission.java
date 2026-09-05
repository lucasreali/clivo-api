package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.practitioner.Practitioner;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "commission")
public class Commission extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false, updatable = false)
    private Long encounterId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false, updatable = false)
    private Practitioner practitioner;

    @Embedded
    private CommissionRate percentage;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;

    @Column(nullable = false, updatable = false)
    private LocalDate period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected Commission() {
    }

    public Commission(Long encounterId, PractitionerCommission earner, Money invoiceAmount, CommissionPeriod period) {
        this.encounterId = encounterId;
        this.practitioner = earner.practitioner();
        this.percentage = earner.rate();
        this.period = period.firstDay();
        this.status = CommissionStatus.OPEN;
        this.amount = calculate(invoiceAmount);
    }

    public Long id() {
        return id;
    }

    public Money calculate(Money invoiceAmount) {
        return percentage.appliedTo(invoiceAmount);
    }

    public Money amount() {
        return amount;
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    public void close() {
        requireOpen();
        status = CommissionStatus.CLOSED;
        closedAt = Instant.now();
    }

    public CommissionSnapshot snapshot() {
        return new CommissionSnapshot(
                id,
                encounterId,
                practitioner.id(),
                practitioner.name(),
                percentage,
                amount,
                CommissionPeriod.covering(period),
                status);
    }

    private void requireOpen() {
        if (isOpen()) {
            return;
        }
        throw new BusinessException("commission %d belongs to a settlement already closed".formatted(id));
    }
}
