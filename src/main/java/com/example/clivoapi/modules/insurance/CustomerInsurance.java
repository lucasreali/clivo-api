package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.customer.Customer;
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
@Table(name = "customer_insurance")
public class CustomerInsurance extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "insurance_plan_id", nullable = false, updatable = false)
    private InsurancePlan plan;

    @Embedded
    private MemberNumber memberNumber;

    protected CustomerInsurance() {
    }

    public CustomerInsurance(Customer customer, InsurancePlan plan, MemberNumber memberNumber) {
        this.customer = customer;
        this.plan = plan;
        this.memberNumber = memberNumber;
    }

    public Long id() {
        return id;
    }

    public boolean isUsable() {
        return plan.isActive() && !plan.reimbursesNothing();
    }

    public Money reimbursementFor(Money grossAmount) {
        return plan.reimbursementFor(grossAmount);
    }

    public String planName() {
        return plan.name();
    }

    public CustomerInsuranceSnapshot snapshot() {
        return new CustomerInsuranceSnapshot(id, customer.id(), plan.snapshot(), memberNumber);
    }
}
