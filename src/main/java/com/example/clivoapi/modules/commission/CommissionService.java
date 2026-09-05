package com.example.clivoapi.modules.commission;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.BillingService;
import com.example.clivoapi.core.practitioner.PractitionerService;
import com.example.clivoapi.modules.commission.internal.CommissionRepository;
import com.example.clivoapi.modules.commission.internal.PractitionerCommissionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommissionService {

    private final CommissionRepository commissions;
    private final PractitionerCommissionRepository earners;
    private final PractitionerService practitioners;
    private final BillingService billing;

    CommissionService(
            CommissionRepository commissions,
            PractitionerCommissionRepository earners,
            PractitionerService practitioners,
            BillingService billing) {
        this.commissions = commissions;
        this.earners = earners;
        this.practitioners = practitioners;
        this.billing = billing;
    }

    public PractitionerCommissionSnapshot chargeAt(Long practitionerId, CommissionRate rate) {
        PractitionerCommission earner = earners
                .findByPractitionerId(practitionerId)
                .orElseGet(() -> new PractitionerCommission(practitioners.reference(practitionerId), rate));
        earner.chargeAt(rate);
        return earners.save(earner).snapshot();
    }

    public Optional<CommissionSnapshot> accrueFor(CompletedEncounter completed) {
        return earners.findByPractitionerId(completed.practitionerId())
                .filter(PractitionerCommission::earnsAnything)
                .map(earner -> accrue(earner, completed));
    }

    public CommissionStatement close(CommissionPeriod period) {
        List<Commission> earned = within(period);
        requireSomethingToClose(earned, period);
        earned.forEach(Commission::close);
        return statementOf(period, commissions.saveAll(earned));
    }

    @Transactional(readOnly = true)
    public CommissionStatement statementOf(CommissionPeriod period) {
        return statementOf(period, within(period));
    }

    @Transactional(readOnly = true)
    public List<PractitionerCommissionSnapshot> rates() {
        return earners.findAll().stream().map(PractitionerCommission::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public CommissionSnapshot findOne(Long id) {
        return commissions.findById(id)
                .map(Commission::snapshot)
                .orElseThrow(() -> new ResourceNotFoundException("Commission", id));
    }

    private CommissionSnapshot accrue(PractitionerCommission earner, CompletedEncounter completed) {
        Money billed = billing.findByEncounter(completed.encounterId()).amounts().net();
        Commission commission = new Commission(
                completed.encounterId(), earner, billed, CommissionPeriod.covering(LocalDate.now()));
        return commissions.save(commission).snapshot();
    }

    private List<Commission> within(CommissionPeriod period) {
        return commissions.findByPeriodOrderByIdAsc(period.firstDay());
    }

    private CommissionStatement statementOf(CommissionPeriod period, List<Commission> earned) {
        return CommissionStatement.of(period, earned.stream().map(Commission::snapshot).toList());
    }

    private void requireSomethingToClose(List<Commission> earned, CommissionPeriod period) {
        if (earned.stream().anyMatch(Commission::isOpen)) {
            return;
        }
        throw new BusinessException("period %s has no open commission to close".formatted(period));
    }
}
