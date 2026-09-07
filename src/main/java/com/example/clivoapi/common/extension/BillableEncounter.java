package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.money.Money;
import java.util.UUID;

public record BillableEncounter(UUID encounterId, UUID customerId, UUID serviceId, Money grossAmount) {
}
