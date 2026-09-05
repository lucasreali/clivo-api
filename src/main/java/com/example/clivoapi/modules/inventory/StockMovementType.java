package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Arrays;

public enum StockMovementType {

    INBOUND {
        @Override
        void applyTo(Product product, Quantity quantity) {
            product.increaseStock(quantity);
        }
    },
    OUTBOUND {
        @Override
        void applyTo(Product product, Quantity quantity) {
            product.decreaseStock(quantity);
        }
    },
    ADJUSTMENT {
        @Override
        void applyTo(Product product, Quantity quantity) {
            product.adjustStockTo(quantity);
        }
    },
    DISCARD {
        @Override
        void applyTo(Product product, Quantity quantity) {
            product.decreaseStock(quantity);
        }
    };

    public static StockMovementType of(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "a stock movement is one of %s".formatted(Arrays.toString(values()))));
    }

    abstract void applyTo(Product product, Quantity quantity);
}
