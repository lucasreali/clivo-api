package com.example.clivoapi.modules.insurance;

import java.util.UUID;

public record InsurancePlanSnapshot(UUID id, PlanDetails details, InsurancePlanStatus status) {
}
