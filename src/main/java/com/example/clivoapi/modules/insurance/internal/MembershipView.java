package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.CustomerInsuranceSnapshot;
import java.util.UUID;

record MembershipView(UUID id, UUID customerId, PlanView plan, String memberNumber) {

    static MembershipView of(CustomerInsuranceSnapshot membership) {
        return new MembershipView(
                membership.id(),
                membership.customerId(),
                PlanView.of(membership.plan()),
                membership.memberNumber().asText());
    }
}
