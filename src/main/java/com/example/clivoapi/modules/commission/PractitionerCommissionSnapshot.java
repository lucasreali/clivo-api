package com.example.clivoapi.modules.commission;

import java.util.UUID;

public record PractitionerCommissionSnapshot(
        UUID id, UUID practitionerId, String practitionerName, CommissionRate rate) {
}
