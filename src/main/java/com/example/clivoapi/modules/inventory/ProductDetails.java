package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.text.TextField;

public record ProductDetails(String name, MeasurementUnit unit, Quantity minimum, boolean batchControlled) {

    private static final TextField NAME = new TextField("a product name", 120);

    public ProductDetails {
        name = NAME.required(name);
    }
}
