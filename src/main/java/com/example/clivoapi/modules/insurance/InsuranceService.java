package com.example.clivoapi.modules.insurance;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.modules.insurance.internal.CustomerInsuranceRepository;
import com.example.clivoapi.modules.insurance.internal.InsurancePlanRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InsuranceService {

    private final InsurancePlanRepository plans;
    private final CustomerInsuranceRepository memberships;
    private final CustomerService customers;

    InsuranceService(
            InsurancePlanRepository plans, CustomerInsuranceRepository memberships, CustomerService customers) {
        this.plans = plans;
        this.memberships = memberships;
        this.customers = customers;
    }

    public InsurancePlanSnapshot register(PlanDetails details) {
        return plans.save(new InsurancePlan(details)).snapshot();
    }

    public InsurancePlanSnapshot describe(UUID id, PlanDetails details) {
        InsurancePlan plan = planOf(id);
        plan.describeAs(details);
        return plans.save(plan).snapshot();
    }

    public InsurancePlanSnapshot deactivate(UUID id) {
        InsurancePlan plan = planOf(id);
        plan.deactivate();
        return plans.save(plan).snapshot();
    }

    public CustomerInsuranceSnapshot enrol(UUID customerId, UUID planId, MemberNumber memberNumber) {
        requireNotEnrolled(customerId, planId);
        CustomerInsurance membership =
                new CustomerInsurance(customers.reference(customerId), planOf(planId), memberNumber);
        return memberships.save(membership).snapshot();
    }

    @Transactional(readOnly = true)
    public List<InsurancePlanSnapshot> catalogue() {
        return plans.findAllByOrderByNameAsc().stream().map(InsurancePlan::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerInsuranceSnapshot> membershipsOf(UUID customerId) {
        return memberships.findByCustomerIdOrderByIdAsc(customerId).stream()
                .map(CustomerInsurance::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CustomerInsurance> usableFor(UUID customerId) {
        return memberships.findByCustomerIdOrderByIdAsc(customerId).stream()
                .filter(CustomerInsurance::isUsable)
                .findFirst();
    }

    private void requireNotEnrolled(UUID customerId, UUID planId) {
        if (memberships.findByCustomerIdAndPlanId(customerId, planId).isEmpty()) {
            return;
        }
        throw new BusinessException("this customer already carries a membership of that plan");
    }

    private InsurancePlan planOf(UUID id) {
        return plans.findById(id).orElseThrow(() -> new ResourceNotFoundException("InsurancePlan", id));
    }
}
