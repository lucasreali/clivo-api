package com.example.clivoapi.modules.insurance;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "insurance_plan")
public class InsurancePlan extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private CoveragePercentage reimbursement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsurancePlanStatus status;

    protected InsurancePlan() {
    }

    public InsurancePlan(PlanDetails details) {
        this.status = InsurancePlanStatus.ACTIVE;
        describeAs(details);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Money reimbursementFor(Money grossAmount) {
        return reimbursement.appliedTo(grossAmount);
    }

    public boolean isActive() {
        return status == InsurancePlanStatus.ACTIVE;
    }

    public boolean reimbursesNothing() {
        return reimbursement.isNone();
    }

    public void describeAs(PlanDetails details) {
        this.name = details.name();
        this.reimbursement = details.reimbursement();
    }

    public void deactivate() {
        status = InsurancePlanStatus.INACTIVE;
    }

    public InsurancePlanSnapshot snapshot() {
        return new InsurancePlanSnapshot(id, new PlanDetails(name, reimbursement), status);
    }
}
