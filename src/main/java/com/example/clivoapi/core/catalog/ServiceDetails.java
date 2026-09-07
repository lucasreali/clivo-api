package com.example.clivoapi.core.catalog;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.common.text.TextField;

public record ServiceDetails(String name, ServiceDuration duration, Money price) {

    private static final TextField NAME = new TextField("a service name", 120);

    public ServiceDetails {
        name = NAME.required(name);
        duration = timed(duration);
        price = priced(price);
    }

    private static ServiceDuration timed(ServiceDuration duration) {
        if (duration != null) {
            return duration;
        }
        throw new BusinessException("durationMinutes is required");
    }

    private static Money priced(Money price) {
        if (price != null) {
            return price;
        }
        throw new BusinessException("price is required");
    }
}
