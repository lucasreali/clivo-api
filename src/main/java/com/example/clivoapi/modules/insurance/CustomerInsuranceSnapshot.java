package com.example.clivoapi.modules.insurance;

public record CustomerInsuranceSnapshot(
        Long id, Long customerId, InsurancePlanSnapshot plan, MemberNumber memberNumber) {
}
