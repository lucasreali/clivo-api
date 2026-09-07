package com.example.clivoapi.modules.insurance;

import java.util.UUID;

public record CustomerInsuranceSnapshot(
        UUID id, UUID customerId, InsurancePlanSnapshot plan, MemberNumber memberNumber) {
}
