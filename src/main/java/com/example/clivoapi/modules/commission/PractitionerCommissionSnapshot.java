package com.example.clivoapi.modules.commission;

public record PractitionerCommissionSnapshot(
        Long id, Long practitionerId, String practitionerName, CommissionRate rate) {
}
