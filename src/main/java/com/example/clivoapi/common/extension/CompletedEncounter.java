package com.example.clivoapi.common.extension;

import java.util.UUID;

public record CompletedEncounter(UUID encounterId, UUID customerId, UUID practitionerId, UUID serviceId) {
}
