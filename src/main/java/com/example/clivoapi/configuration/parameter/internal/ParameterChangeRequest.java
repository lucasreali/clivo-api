package com.example.clivoapi.configuration.parameter.internal;

import jakarta.validation.constraints.NotNull;

record ParameterChangeRequest(@NotNull String value) {
}
