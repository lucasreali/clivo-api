package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.common.extension.BillableEncounter;
import com.example.clivoapi.common.extension.InvoiceAdjuster;
import com.example.clivoapi.common.extension.InvoiceAdjustment;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.insurance.CustomerInsurance;
import com.example.clivoapi.modules.insurance.InsuranceService;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class InsuranceInvoiceAdjuster implements InvoiceAdjuster {

    private static final String INSURANCE_COVERAGE = "INSURANCE";
    private static final ModuleCode INSURANCE = new ModuleCode("insurance");

    private final ModuleActivationState activation;
    private final InsuranceService insurance;

    InsuranceInvoiceAdjuster(ModuleActivationState activation, InsuranceService insurance) {
        this.activation = activation;
        this.insurance = insurance;
    }

    @Override
    public Optional<InvoiceAdjustment> adjustmentFor(BillableEncounter encounter) {
        return membershipFor(encounter).map(membership -> reimbursementOf(membership, encounter));
    }

    private Optional<CustomerInsurance> membershipFor(BillableEncounter encounter) {
        if (activation.isActive(INSURANCE)) {
            return insurance.usableFor(encounter.customerId());
        }
        return Optional.empty();
    }

    private InvoiceAdjustment reimbursementOf(CustomerInsurance membership, BillableEncounter encounter) {
        return new InvoiceAdjustment(
                INSURANCE_COVERAGE,
                membership.reimbursementFor(encounter.grossAmount()),
                "reimbursed by %s".formatted(membership.planName()));
    }
}
