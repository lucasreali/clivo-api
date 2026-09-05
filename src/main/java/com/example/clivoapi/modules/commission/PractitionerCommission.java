package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.practitioner.Practitioner;
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
@Table(name = "commission_rate")
public class PractitionerCommission extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "practitioner_id", nullable = false, updatable = false)
    private Practitioner practitioner;

    @Embedded
    private CommissionRate rate;

    protected PractitionerCommission() {
    }

    public PractitionerCommission(Practitioner practitioner, CommissionRate rate) {
        this.practitioner = practitioner;
        this.rate = rate;
    }

    public Long id() {
        return id;
    }

    public Practitioner practitioner() {
        return practitioner;
    }

    public CommissionRate rate() {
        return rate;
    }

    public boolean earnsAnything() {
        return !rate.isNone();
    }

    public void chargeAt(CommissionRate newRate) {
        rate = newRate;
    }

    public Money shareOf(Money amount) {
        return rate.appliedTo(amount);
    }

    public PractitionerCommissionSnapshot snapshot() {
        return new PractitionerCommissionSnapshot(id, practitioner.id(), practitioner.name(), rate);
    }
}
