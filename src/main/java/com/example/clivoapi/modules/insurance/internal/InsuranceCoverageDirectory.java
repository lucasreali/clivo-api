package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.common.extension.CoverageDirectory;
import com.example.clivoapi.common.extension.CoverageNote;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.modules.insurance.CustomerInsurance;
import com.example.clivoapi.modules.insurance.CustomerInsuranceSnapshot;
import com.example.clivoapi.modules.insurance.InsuranceService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class InsuranceCoverageDirectory implements CoverageDirectory {

    private static final ModuleCode INSURANCE = new ModuleCode("insurance");

    private final ModuleActivationState activation;
    private final InsuranceService insurance;

    InsuranceCoverageDirectory(ModuleActivationState activation, InsuranceService insurance) {
        this.activation = activation;
        this.insurance = insurance;
    }

    @Override
    public Optional<CoverageNote> coverageOf(UUID customerId) {
        return membershipFor(customerId).map(CustomerInsurance::snapshot).map(this::noteOf);
    }

    private Optional<CustomerInsurance> membershipFor(UUID customerId) {
        if (activation.isActive(INSURANCE)) {
            return insurance.usableFor(customerId);
        }
        return Optional.empty();
    }

    private CoverageNote noteOf(CustomerInsuranceSnapshot membership) {
        return new CoverageNote(membership.plan().details().name(), membership.memberNumber().asText());
    }
}
