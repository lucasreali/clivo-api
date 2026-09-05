package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.CustomerInsuranceSnapshot;

record MembershipView(Long id, Long customerId, PlanView plan, String memberNumber) {

    static MembershipView of(CustomerInsuranceSnapshot membership) {
        return new MembershipView(
                membership.id(),
                membership.customerId(),
                PlanView.of(membership.plan()),
                membership.memberNumber().asText());
    }
}
