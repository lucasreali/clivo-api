package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.extension.ParameterValue;

public enum ParameterDataType {

    INTEGER {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsWholeNumber(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "a whole number %s".formatted(constraints.rangeDescription());
        }
    },

    DECIMAL {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsDecimal(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "a decimal number %s".formatted(constraints.rangeDescription());
        }
    },

    BOOLEAN {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsFlag(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "true or false";
        }
    },

    ENUM {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsOption(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "one of %s".formatted(constraints.optionsDescription());
        }
    },

    LIST {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsItems(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "a comma separated selection of %s".formatted(constraints.optionsDescription());
        }
    },

    REF {
        @Override
        boolean accepts(ParameterValue value, ParameterConstraints constraints) {
            return constraints.acceptsReference(value);
        }

        @Override
        String acceptedValues(ParameterConstraints constraints) {
            return "an identifier";
        }
    };

    abstract boolean accepts(ParameterValue value, ParameterConstraints constraints);

    abstract String acceptedValues(ParameterConstraints constraints);
}
