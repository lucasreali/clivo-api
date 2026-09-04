package com.example.clivoapi.core.catalog;

import com.example.clivoapi.common.money.Money;

public record ServiceDetails(String name, ServiceDuration duration, Money price) {
}
