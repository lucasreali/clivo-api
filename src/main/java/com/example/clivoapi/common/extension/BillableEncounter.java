package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.money.Money;

public record BillableEncounter(Long encounterId, Long customerId, Long serviceId, Money grossAmount) {
}
